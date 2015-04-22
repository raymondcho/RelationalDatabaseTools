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
		outputMsg = "Decomposing input relation into 3NF relations using the Synthesis algorithm. ";
		List<Relation> workingOutputRelations = new ArrayList<>();
		// Obtain list of all attributes in original relation
		List<Attribute> originalAttributes = originalRelation.getAttributes();
		List<Attribute> addedAttributes = new ArrayList<>();
		// Obtain minimal (canonical) cover of the set of original relation's functional dependencies.
		// For each functional dependency, create a relation schema with the attributes in that functional dependency (both sides).
		outputMsg += "For each functional dependency of the canonical cover set of original relation's functional dependencies, "
				+ "create a relation schema with the attributes in that functional dependency (both sides). ";
		int counter = 0;
		for (FunctionalDependency fd : originalRelation.getMinimalCover()) {
			List<Attribute> decomposedAttrs = new ArrayList<>();
			decomposedAttrs.addAll(fd.getLeftHandAttributes());
			decomposedAttrs.addAll(fd.getRightHandAttributes());
			List<FunctionalDependency> decomposedFD = new ArrayList<>();
			decomposedFD.add(fd);
			Relation threeNFRelation = new Relation(originalRelation.getName() + counter++, decomposedAttrs, decomposedFD);
			for (Attribute a : decomposedAttrs) {
				if (!CalculateClosure.containsAttribute(addedAttributes, a)) {
					addedAttributes.add(a);
				}
			}
			workingOutputRelations.add(threeNFRelation);
		}
		// Place any remaining attributes that have not been placed in any relations in the previous step in a single relation schema.
		if (originalAttributes.size() > addedAttributes.size()) {
			outputMsg += " There is at least one attribute from original relation that has not been placed in any new relation, " 
					+ "so creating additional relation schema with those attribute(s):";
			List<Attribute> missingAttributes = new ArrayList<>();
			for (Attribute missingAttr : originalAttributes) {
				if (!CalculateClosure.containsAttribute(addedAttributes, missingAttr)) {
					outputMsg += " " + missingAttr.getName();
					missingAttributes.add(missingAttr);
				}
			}
			outputMsg += ". ";
			List<FunctionalDependency> emptyFD = new ArrayList<>();
			Relation extra3NFRelation = new Relation(originalRelation.getName() + counter++, missingAttributes, emptyFD);
			workingOutputRelations.add(extra3NFRelation);
		}
		// Check if a key of the original relation is found in at least one newly formed 3NF relation.
		for (Closure minimumKey : originalRelation.getMinimumKeyClosures()) {
			boolean containsAllAttributes = false;
			for (Relation r : workingOutputRelations) {
				boolean relationHasAllAttrs = true;
				for (Attribute minimumKeyAttr : minimumKey.getClosureOf()) {
					if (!CalculateClosure.containsAttribute(r.getAttributes(), minimumKeyAttr)) {
						relationHasAllAttrs = false;
						break;
					}
				}
				if (relationHasAllAttrs) {
					containsAllAttributes = true;
					break;
				}
			}
			if (!containsAllAttributes) {
				// Create an additional relation schema with the missing key.
				outputMsg += " Key {" + minimumKey.printLeftSideAttributes() + "} was not found in any of the newly formed 3NF relations, "
						+ " therefore creating an additional relation schema with that key. ";
				List<FunctionalDependency> emptyFD = new ArrayList<>();
				Relation keyRelation = new Relation(originalRelation.getName() + counter++, minimumKey.getClosureOf(), emptyFD);
				workingOutputRelations.add(keyRelation);
			}
		}
		// Finally, if any relation includes only a subset of attributes found in another relation, delete the smaller relation.
		outputMsg += " Testing if any relation includes all of the attributes found in another relation "
				+ "(and deleting the duplicate or smaller one). ";
		boolean[] removeIndices = new boolean[workingOutputRelations.size()];
		boolean removedone = false;
		for (int i = 0; i < removeIndices.length; i++) {
			removeIndices[i] = false;
		}
		for (int i = 0; i < workingOutputRelations.size(); i++) {
			if (!removeIndices[i]) {
				Relation currentRelation = workingOutputRelations.get(i);
				for (int j = 0; j < workingOutputRelations.size(); j++) {
					if (i != j && !removeIndices[j]) {
						Relation otherRelation = workingOutputRelations.get(j);
						if (CalculateClosure.isProperSubsetAttributeOf(currentRelation.getAttributes(), otherRelation.getAttributes())) {
							removeIndices[j] = true;
							removedone = true;
						}
					}
				}
			}
		}
		if (removedone) {
			outputMsg += " Removed at least one new relation that was a duplicate or subset of another new relation.";
		} else {
			outputMsg += " No new relations were removed.";
		}
		for (int i = 0; i < workingOutputRelations.size(); i++) {
			if (!removeIndices[i]) {
				threeNFRelations.add(workingOutputRelations.get(i));
			}
		}
		outputMsg += " Finished decomposing input relation into 3NF relations.";
		outputMsgFlag = true;
	}
	
	protected List<Relation> get3NFRelations() {
		return threeNFRelations;
	}
}
