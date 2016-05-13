package relationalDatabaseTools.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to decompose a relation not in Third normal form into relations that are
 * in Third normal form.
 * 
 * @author Raymond Cho
 * 
 */
public class Calculate3NFDecomposition extends CalculateDecomposition {

	public Calculate3NFDecomposition(final Relation inputRelation) {
		super(inputRelation);
	}

	@Override
	protected void decompose() {
		decompose(false);
	}
	
	protected void force3NFDecomposition() {
		decompose(true);
	}
	
	private void decompose(final boolean force3NFDecomposition) {
		if (getInputRelation().getMinimalCover().isEmpty()) {
			setOutputMsgFlag(true);
			setOutputMsg("No functional dependencies in minimal cover, therefore input relation is already in 3NF.");
			return;
		}
		if (getInputRelation().getNormalFormsResults().isIn3NF() && !force3NFDecomposition) {
			setOutputMsgFlag(true);
			setOutputMsg("Input relation is already in 3NF. No decomposition necessary. ");
			return;
		}
		setOutputMsg("Decomposing input relation into 3NF relations using the Synthesis algorithm.");
		List<Relation> workingOutputRelations = new ArrayList<>();
		// Obtain list of all attributes in original relation
		List<Attribute> originalAttributes = getInputRelation().getAttributes();
		List<Attribute> addedAttributes = new ArrayList<>();
		// Obtain minimal (canonical) cover of the set of original relation's
		// functional dependencies.
		// For each functional dependency, create a relation schema with the
		// attributes in that functional dependency (both sides).
		appendOutputMsg(" For each functional dependency of the canonical cover set (merging functional dependencies having the same left-hand attribute(s)) of original relation's functional dependencies, "
				+ "create a relation schema with the attributes in that functional dependency (both sides).");
		int counter = 0;
		for (FunctionalDependency fd : getInputRelation().getMinimalCover()) {
			List<Attribute> decomposedAttrs = new ArrayList<>();
			decomposedAttrs.addAll(fd.getLeftHandAttributes());
			decomposedAttrs.addAll(fd.getRightHandAttributes());
			List<FunctionalDependency> decomposedFD = RDTUtils.fetchFDsOfDecomposedR(RDTUtils.getSingleAttributeMinimalCoverList(getInputRelation().getMinimalCover(), getInputRelation()),
					decomposedAttrs);
			Relation threeNFRelation = new Relation(getInputRelation().getName() + counter++, decomposedAttrs, decomposedFD);
			for (Attribute a : decomposedAttrs) {
				if (!RDTUtils.attributeListContainsAttribute(addedAttributes, a)) {
					addedAttributes.add(a);
				}
			}
			workingOutputRelations.add(threeNFRelation);
		}
		// Place any remaining attributes that have not been placed in any
		// relations in the previous step in a single relation schema.
		if (originalAttributes.size() > addedAttributes.size()) {
			appendOutputMsg(" There is at least one attribute from original relation that has not been placed in any new relation, "
					+ "so creating additional relation schema with those attribute(s):");
			List<Attribute> missingAttributes = new ArrayList<>();
			for (Attribute missingAttr : originalAttributes) {
				if (!RDTUtils.attributeListContainsAttribute(addedAttributes, missingAttr)) {
					appendOutputMsg(" " + missingAttr.getName());
					missingAttributes.add(missingAttr);
				}
			}
			appendOutputMsg(". ");
			List<FunctionalDependency> emptyFD = RDTUtils.fetchFDsOfDecomposedR(RDTUtils.getSingleAttributeMinimalCoverList(getInputRelation().getMinimalCover(), getInputRelation()),
					missingAttributes);
			Relation extra3NFRelation = new Relation(getInputRelation().getName() + counter++, missingAttributes, emptyFD);
			workingOutputRelations.add(extra3NFRelation);
		}
		// If none of the new relations is a superkey for the original R, then
		// add another relation whose schema is a key for R.
		appendOutputMsg(" Checking if at least one key can be found in at least one newly formed 3NF relation.");
		Closure foundKey = null;
		for (Closure minimumKey : getInputRelation().getMinimumKeyClosures()) {
			for (Relation r : workingOutputRelations) {
				if (RDTUtils.isAttributeListSubsetOfOtherAttributeList(r.getAttributes(), minimumKey.getClosureOf())) {
					foundKey = minimumKey;
					break;
				}
			}
			if (foundKey != null) {
				break;
			}
		}
		if (foundKey != null) {
			appendOutputMsg(" Since key {" + foundKey.printLeftSideAttributes() + 
					"} is present in at least one of the new 3NF relations, no new relation was created.");
		} else {
			appendOutputMsg(" Since none of the newly created 3NF relations contains a key of the original relation, need to "
					+ "add another relation whose schema is a key of the original relation.");
			List<Attribute> addedKeyAttrs = getInputRelation().getMinimumKeyClosures().get(0).getClosureOf();
			List<FunctionalDependency> emptyFD = RDTUtils.fetchFDsOfDecomposedR(RDTUtils.getSingleAttributeMinimalCoverList(getInputRelation().getMinimalCover(), getInputRelation()), addedKeyAttrs);
			Relation keyRelation = new Relation(getInputRelation().getName() + counter++, addedKeyAttrs, emptyFD);
			appendOutputMsg(" Added key {" + getInputRelation().getMinimumKeyClosures().get(0).printLeftSideAttributes() + "}. ");
			workingOutputRelations.add(keyRelation);
		}
		// Finally, if any relation includes only a subset of attributes found
		// in another relation, delete the smaller relation.
		appendOutputMsg(" Testing if any relation includes all of the attributes found in another relation "
				+ "(and deleting the duplicate or smaller one).");
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
						if (RDTUtils.isAttributeListSubsetOfOtherAttributeList(currentRelation.getAttributes(),
								otherRelation.getAttributes())) {
							removeIndices[j] = true;
							removedone = true;
						}
					}
				}
			}
		}
		if (removedone) {
			appendOutputMsg(" Removed at least one new relation that was a duplicate or subset of another new relation.");
		} else {
			appendOutputMsg(" No new relations were removed.");
		}
		for (int i = 0; i < workingOutputRelations.size(); i++) {
			if (!removeIndices[i]) {
				addRelationtoOutputList(workingOutputRelations.get(i));
			}
		}
		appendOutputMsg(" Finished decomposing input relation into 3NF relations: ");
		setOutputMsgFlag(true);
		return;
	}
}
