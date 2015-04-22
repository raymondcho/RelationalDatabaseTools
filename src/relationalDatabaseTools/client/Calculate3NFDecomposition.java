package relationalDatabaseTools.client;

import java.util.ArrayList;
import java.util.List;

public class Calculate3NFDecomposition {
	private final Relation originalRelation;
	private final List<Relation> threeNFRelations;
	private boolean outputMsgFlag;
	private String outputMsg;
	
	public Calculate3NFDecomposition(final Relation inputRelation) {
		this.originalRelation = inputRelation;
		threeNFRelations = new ArrayList<Relation>();
		outputMsgFlag = false;
		outputMsg = "";
	}
	
	public boolean hasOutputMsg() {
		return outputMsgFlag;
	}
	
	public String getOutputMsg() {
		return outputMsg;
	}
	
	public void decomposeTo3NF() {
		if (originalRelation.getMinimalCover().isEmpty()) {
			outputMsgFlag = true;
			outputMsg = "No functional dependencies in minimal cover, therefore input relation is already in 3NF.";
			return;
		}
		outputMsg = "Decomposing input relation into 3NF relations using the Synthesis algorithm.";
		// Obtain list of all attributes in original relation
		List<Attribute> originalAttributes = originalRelation.getAttributes();
		List<Attribute> addedAttributes = new ArrayList<>();
		// Obtain minimal (canonical) cover of the set of original relation's functional dependencies.
		// For each functional dependency, create a relation schema with the closure of the left-hand side.
		int counter = 0;
		for (FunctionalDependency fd : originalRelation.getMinimalCover()) {
			// Find the closure of this FD.
			Closure closure = null;
			for (Closure c : originalRelation.getClosures()) {
				if (c.getClosureOf().size() == fd.getLeftHandAttributes().size()) {
					boolean containsAll = true;
					for (Attribute a : fd.getLeftHandAttributes()) {
						if (!CalculateClosure.containsAttribute(c.getClosureOf(), a)) {
							containsAll = false;
							break;
						}
					}
					if (containsAll) {
						closure = c;
						break;
					}
				}
			}
			List<FunctionalDependency> decomposedFD = new ArrayList<>();
			decomposedFD.add(fd);
			Relation threeNFRelation = new Relation(originalRelation.getName() + counter++, closure.getClosure(), decomposedFD);
			CalculateClosure.improvedCalculateClosures(threeNFRelation);
			for (Attribute a : closure.getClosure()) {
				if (!CalculateClosure.containsAttribute(addedAttributes, a)) {
					addedAttributes.add(a);
				}
			}
			threeNFRelations.add(threeNFRelation);
		}
		// Place any remaining attributes that have not been placed in any relations in the previous step in a single relation schema.
		if (originalAttributes.size() > addedAttributes.size()) {
			List<Attribute> missingAttributes = new ArrayList<>();
			for (Attribute missingAttr : originalAttributes) {
				if (!CalculateClosure.containsAttribute(addedAttributes, missingAttr)) {
					missingAttributes.add(missingAttr);
				}
			}
			List<FunctionalDependency> emptyFD = new ArrayList<>();
			Relation extra3NFRelation = new Relation(originalRelation.getName() + counter++, missingAttributes, emptyFD);
			CalculateClosure.improvedCalculateClosures(extra3NFRelation);
			threeNFRelations.add(extra3NFRelation);
		}
		// Check if a key of the original relation is found in at least one newly formed 3NF relation.
		boolean originalKeyFound = false;
		for (Closure minimumKey : originalRelation.getMinimumKeyClosures()) {
			boolean containsAllAttributes = true;
			for (Relation r : threeNFRelations) {
				for (Attribute minimumKeyAttr : minimumKey.getClosureOf()) {
					if (!CalculateClosure.containsAttribute(r.getAttributes(), minimumKeyAttr)) {
						containsAllAttributes = false;
						break;
					}
				}
				if (containsAllAttributes) {
					originalKeyFound = true;
					break;
				}
			}
			if (originalKeyFound) {
				break;
			}
		}
		if (!originalKeyFound) {
			// Create an additional relation schema with a key.
			List<Attribute> keyAttrs = originalRelation.getMinimumKeyClosures().get(0).getClosureOf();
			List<FunctionalDependency> emptyFD = new ArrayList<>();
			Relation keyRelation = new Relation(originalRelation.getName() + counter++, keyAttrs, emptyFD);
			CalculateClosure.improvedCalculateClosures(keyRelation);
			threeNFRelations.add(keyRelation);
		}
	}
}
