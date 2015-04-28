package relationalDatabaseTools.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to decompose a relation not in Boyce-Codd normal form into a collection
 * of relations that are in Boyce-Codd normal form.
 * 
 * @author Raymond Cho
 * 
 */
public class CalculateBCNFDecomposition extends CalculateDecomposition {
	private final List<Relation> resultWithPossibleDuplicates;

	public CalculateBCNFDecomposition(Relation inputRelation) {
		super(inputRelation);
		resultWithPossibleDuplicates = new ArrayList<>();
	}

	@Override
	protected void decompose() {
		if (getInputRelation().getMinimalCover().isEmpty()) {
			MinimalFDCover.determineMinimalCover(getInputRelation());
			if (getInputRelation().getMinimalCover().isEmpty()) {
				setOutputMsgFlag(true);
				setOutputMsg("No functional dependencies in minimal cover, therefore input relation is already in BCNF.");
				return;
			}
		}
		if (getInputRelation().getNormalFormsResults().isInBCNF()) {
			setOutputMsgFlag(true);
			setOutputMsg("Input relation is already in BCNF. No decomposition necessary. ");
			return;
		}
		setOutputMsg("Decomposing input relation into BCNF relations. ");
		List<Relation> workingOutputRelations = decomposeBCNFHelper(getInputRelation());
		resultWithPossibleDuplicates.addAll(workingOutputRelations);
		boolean[] removeIndices = new boolean[workingOutputRelations.size()];
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
						}
					}
				}
			}
		}
		for (int i = 0; i < workingOutputRelations.size(); i++) {
			if (!removeIndices[i]) {
				addRelationtoOutputList(workingOutputRelations.get(i));
			}
		}
		List<FunctionalDependency> missingFDs = new ArrayList<>();
		for (FunctionalDependency originalFD : getInputRelation().getFDs()) {
			String originalFDname = originalFD.getFDName();
			boolean found = false;
			for (Relation bcnfR : getOutputRelations()) {
				for (FunctionalDependency outputFD : bcnfR.getInputFDs()) {
					String outputFDname = outputFD.getFDName();
					if (originalFDname.equals(outputFDname)) {
						found = true;
						break;
					}
				}
			}
			if (!found) {
				missingFDs.add(originalFD);
			}
		}
		appendOutputMsg(" Finished decomposing input relation into BCNF relations:");
		if (missingFDs.isEmpty()) {
			appendOutputMsg(" (No functional dependencies were lost) ");
		} else {
			appendOutputMsg(" Warning: the following functional dependencies were lost: ");
			for (int i = 0; i < missingFDs.size(); i++) {
				appendOutputMsg(missingFDs.get(i).getFDName());
				if (i < missingFDs.size() - 1) {
					appendOutputMsg("; ");
				}
			}
			appendOutputMsg(". ");
		}
		setOutputMsgFlag(true);
		return;
	}
	
	private List<Relation> decomposeBCNFHelper(final Relation r) {
		List<Relation> result = new ArrayList<>();
		int counter = 0;
		if (r.getClosures().isEmpty()) {
			CalculateClosure.improvedCalculateClosures(r);
		}
		if (r.getMinimalCover().isEmpty()) {
			MinimalFDCover.determineMinimalCover(r);
		}
		if (r.getMinimumKeyClosures().isEmpty()) {
			CalculateKeys.calculateKeys(r);
		}
		if (!r.getNormalFormsResults().hasDeterminedNormalForms) {
			r.determineNormalForms();
		}
		if (r.getNormalFormsResults().isInBCNF()) {
			result.add(r);
			return result;
		}
		for (FunctionalDependency f : r.getNormalFormsResults().getBCNFViolatingFDs()) {
			Closure leftSideClosure = RDTUtils.findClosureWithLeftHandAttributes(f.getLeftHandAttributes(), r.getClosures());
			List<FunctionalDependency> r1FDs = RDTUtils.fetchFDsOfDecomposedR(r.getMinimalCover(), leftSideClosure.getClosure());
			Relation r1 = new Relation(r.getName() + "_" + counter++, leftSideClosure.getClosure(), r1FDs);
			List<Attribute> r2Attributes = new ArrayList<>();
			for (Attribute a : f.getLeftHandAttributes()) {
				if (!RDTUtils.attributeListContainsAttribute(r2Attributes, a)) {
					r2Attributes.add(a);
				}
			}
			for (Attribute a : r.getAttributes()) {
				if (!RDTUtils.attributeListContainsAttribute(leftSideClosure.getClosure(), a)) {
					if (!RDTUtils.attributeListContainsAttribute(r2Attributes, a)) {
						r2Attributes.add(a);
					}
				}
			}
			List<FunctionalDependency> r2FDs = RDTUtils.fetchFDsOfDecomposedR(r.getMinimalCover(), r2Attributes);
			Relation r2 = new Relation(r.getName() + "_" + counter++, r2Attributes, r2FDs);
			result.addAll(decomposeBCNFHelper(r1));
			result.addAll(decomposeBCNFHelper(r2));
		}
		return result;
	}

	protected List<Relation> getResultWithDuplicates() {
		return resultWithPossibleDuplicates;
	}
}
