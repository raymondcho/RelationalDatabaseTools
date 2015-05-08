package relationalDatabaseTools.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to decompose a relation not in Boyce-Codd normal form into a collection
 * of relations that are in Boyce-Codd normal form.
 * 
 * The process will attempt two parallel decompositions: one using the input relation as the source.
 * And if a 3NF decomposition was performed (such as if original relation was not in 3NF), then the second method
 * uses the decomposed 3NF relations as the sources. This sometimes makes a difference in the output in terms of
 * minimizing lost functional dependencies and number of decomposed relations.
 * 
 * @author Raymond Cho
 * 
 */
public class CalculateBCNFDecomposition extends CalculateDecomposition {
	private final List<Relation> resultWithPossibleDuplicates;
	private Calculate3NFDecomposition threenfDecomposition;
	private List<Relation> bcnfDecomposedWithDuplicates;
	private List<Relation> threeNFDecomposedWithDuplicates;
	private List<Relation> pureBCNFDecomposedRs;
	private List<Relation> threeNFDecomposedRs;
	private List<FunctionalDependency> pureBCNFLostFDs;
	private List<FunctionalDependency> threeNFLostFDs;
	
	public CalculateBCNFDecomposition(final Calculate3NFDecomposition threenfDecomposition) {
		super(threenfDecomposition.getInputRelation());
		resultWithPossibleDuplicates = new ArrayList<>();
		this.threenfDecomposition = threenfDecomposition;
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
		
		BCNFDecomposeMethodWithout3NF();
		if (!threenfDecomposition.getOutputRelations().isEmpty()) {
			decomposeFrom3NF();
		}
		
		List<FunctionalDependency> missingFDs = null;
		if (threenfDecomposition.getOutputRelations().isEmpty()) {
			setOutputMsg("Decomposing input relation into BCNF relations using input relation as source. ");
			for (Relation outputR : pureBCNFDecomposedRs) {
				addRelationtoOutputList(outputR);
			}
			resultWithPossibleDuplicates.addAll(bcnfDecomposedWithDuplicates);
			missingFDs = pureBCNFLostFDs;
		} else if (threeNFLostFDs.size() < pureBCNFLostFDs.size()) {
			setOutputMsg("Decomposing input relation into BCNF relations using decomposed 3NF relations as sources. ");
			for (Relation outputR : threeNFDecomposedRs) {
				addRelationtoOutputList(outputR);
			}
			resultWithPossibleDuplicates.addAll(threeNFDecomposedWithDuplicates);
			missingFDs = threeNFLostFDs;
		} else if (threeNFLostFDs.size() > pureBCNFLostFDs.size()) {
			setOutputMsg("Decomposing input relation into BCNF relations using input relation as source. ");
			for (Relation outputR : pureBCNFDecomposedRs) {
				addRelationtoOutputList(outputR);
			}
			resultWithPossibleDuplicates.addAll(bcnfDecomposedWithDuplicates);
			missingFDs = pureBCNFLostFDs;
		} else {
			if (threeNFDecomposedRs.size() <= pureBCNFDecomposedRs.size()) {
				setOutputMsg("Decomposing input relation into BCNF relations using decomposed 3NF relations as sources. ");
				for (Relation outputR : threeNFDecomposedRs) {
					addRelationtoOutputList(outputR);
				}
				resultWithPossibleDuplicates.addAll(threeNFDecomposedWithDuplicates);
				missingFDs = threeNFLostFDs;
			} else {
				setOutputMsg("Decomposing input relation into BCNF relations using input relation as source. ");
				for (Relation outputR : pureBCNFDecomposedRs) {
					addRelationtoOutputList(outputR);
				}
				resultWithPossibleDuplicates.addAll(bcnfDecomposedWithDuplicates);
				missingFDs = pureBCNFLostFDs;
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
	
	private void BCNFDecomposeMethodWithout3NF() {
		List<Relation> workingOutputRelations = decomposeBCNFHelper(getInputRelation());
		bcnfDecomposedWithDuplicates = workingOutputRelations;
		List<Relation> eliminatedDuplicatesAndSubsets = eliminateDuplicateSubsetRelations(workingOutputRelations);
		List<FunctionalDependency> missingFDs = findEliminatedFunctionalDependencies(eliminatedDuplicatesAndSubsets);
		pureBCNFDecomposedRs = eliminatedDuplicatesAndSubsets;
		pureBCNFLostFDs = missingFDs;
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
	
	private void decomposeFrom3NF() {
		if (this.threenfDecomposition == null) {
			return;
		}
		List<Relation> workingBCNFRelations = new ArrayList<>();
		for (Relation threeNF : threenfDecomposition.getOutputRelations()) {
			workingBCNFRelations.addAll(decomposeBCNFHelper(threeNF));
		}
		threeNFDecomposedWithDuplicates = workingBCNFRelations;
		List<Relation> purgeDuplicatesAndSubsets = eliminateDuplicateSubsetRelations(workingBCNFRelations);
		List<FunctionalDependency> lostFDs = findEliminatedFunctionalDependencies(purgeDuplicatesAndSubsets);
		threeNFDecomposedRs = purgeDuplicatesAndSubsets;
		threeNFLostFDs = lostFDs;
	}
	
	private List<Relation> eliminateDuplicateSubsetRelations(final List<Relation> workingOutputRelations) {
		List<Relation> output = new ArrayList<>();
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
				output.add(workingOutputRelations.get(i));
			}
		}
		return output;
	}
	
	private List<FunctionalDependency> findEliminatedFunctionalDependencies(final List<Relation> outputRelations) {
		List<FunctionalDependency> missingFDs = new ArrayList<>();
		for (FunctionalDependency originalFD : getInputRelation().getFDs()) {
			String originalFDname = originalFD.getFDName();
			boolean found = false;
			for (Relation bcnfR : outputRelations) {
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
		return missingFDs;
	}

	protected List<Relation> getResultWithDuplicates() {
		return resultWithPossibleDuplicates;
	}
}
