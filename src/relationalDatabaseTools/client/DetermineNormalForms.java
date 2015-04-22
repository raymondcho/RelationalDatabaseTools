package relationalDatabaseTools.client;

import java.util.ArrayList;
import java.util.List;

public class DetermineNormalForms {
	private final Relation relation;
	private boolean isFirstNormalForm;
	private String firstNormalFormMsg;
	private boolean isSecondNormalForm;
	private String secondNormalFormMsg;
	private boolean isThirdNormalForm;
	private String thirdNormalFormMsg;
	private boolean isBCNF;
	private String BCNFMsg;

	public DetermineNormalForms(final Relation relation) {
		this.relation = relation;
	}

	public void calculateNormalForms() {
		calculateFirstNormalForm();
		calculateSecondNormalForm();
		calculateThirdNormalForm();
		calculateBCNF();
	}

	private void calculateFirstNormalForm() {
		isFirstNormalForm = true;
		firstNormalFormMsg = "Input relation is assumed to be in 1NF: each attribute is assumed to contain only one value per row.";
	}

	private void calculateSecondNormalForm() {
		if (!isFirstNormalForm) {
			isSecondNormalForm = false;
			secondNormalFormMsg = "Input relation is not in 2NF because it is not in 1NF.";
			return;
		}
		// Check if all minimum keys are single-attributes
		boolean singleAttribute = true;
		for (Closure c : relation.getMinimumKeyClosures()) {
			if (c.getClosureOf().size() > 1) {
				singleAttribute = false;
				break;
			}
		}
		if (singleAttribute) {
			isSecondNormalForm = true;
			secondNormalFormMsg = "Input relation is in 2NF: " 
					+ "It is in 1NF and there are no composite minimum keys (minimum keys composed of more than one attribute).";
			return;
		}
		// Check if there is at least one non-prime attribute that does not
		// depend on all minimum key attributes
		
		List<Attribute> failedAttrs = new ArrayList<>();
		List<Closure> failedClosures = new ArrayList<>();
		List<Closure> failedProperClosure = new ArrayList<>();
		
		List<Attribute> primeAttributes = relation.getPrimeAttributes();
		for (Closure minClosure : relation.getMinimumKeyClosures()) {
			if (minClosure.getClosureOf().size() > 1) {
				List<Attribute> nonPrimes = new ArrayList<>();
				for (Attribute ab : minClosure.getClosure()) {
					if (!CalculateClosure.containsAttribute(primeAttributes, ab)) {
						nonPrimes.add(ab);
					}
				}
				for (Attribute ac : nonPrimes) {
					for (Closure c : relation.getClosures()) {
						if (c.getClosureOf().size() >= minClosure.getClosureOf().size()) {
							break;
						}
						if (CalculateClosure.isProperSubsetClosureOf(minClosure, c)) {
							if (CalculateClosure.containsAttribute(c.getClosure(), ac) && !CalculateClosure.containsAttribute(c.getClosureOf(), ac)) {
								if (!CalculateClosure.containsAttribute(failedAttrs, ac)) {
									failedAttrs.add(ac);
									failedClosures.add(c);
									failedProperClosure.add(minClosure);
								}
								break;
							}
						}
					}
				}
			}
		}
		
		if (failedAttrs.isEmpty()) {
			isSecondNormalForm = true;
			secondNormalFormMsg = "Input relation is in 2NF: "
					+ "It is in 1NF and there are no partial dependencies on a composite minimum key "
					+ "(a minimum key composed of more than one attribute).";
		} else {
			isSecondNormalForm = false;
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < failedAttrs.size(); i++) {
				sb.append("The minimum set of attributes that attribute " + failedAttrs.get(i).getName() 
						+ " is functionally determined by is attribute(s) {");
				for (int j = 0; j < failedClosures.get(i).getClosureOf().size(); j++) {
					sb.append(failedClosures.get(i).getClosureOf().get(j).getName());
					if (j < failedClosures.get(i).getClosureOf().size() - 1) {
						sb.append(", ");
					}
				}
				sb.append("}; whereas it should only be functionally determined by the full set of attributes of the composite minimum key {");
				for (int k = 0; k < failedProperClosure.get(i).getClosureOf().size(); k++) {
					sb.append(failedProperClosure.get(i).getClosureOf().get(k).getName());
					if (k < failedProperClosure.get(i).getClosureOf().size() - 1) {
						sb.append(", ");
					}
				}
				sb.append("}. ");
				String attribute;
				if (failedAttrs.size() == 1) {
					attribute = "attribute violates";
				} else {
					attribute = "attributes violate";
				}
				secondNormalFormMsg = "Input relation is not in 2NF: There is at least one partial dependency on a composite minimum key. "
						+ "To satisfy 2NF, there should not be any non-prime attribute "
						+ " that can be functionally determined by a proper subset of a composite minimum key. "
						+ "See above closure list for the composite minimum key(s). "
						+ "The following non-prime " + attribute + " the condition: " + sb.toString();
			}
		}
	}

	/*
	 * Determines if for a given FD A-> B, B is a subset of A.
	 */
	private boolean isFDTrivial(final FunctionalDependency f) {
		for (Attribute rightAttr : f.getRightHandAttributes()) {
			if (!CalculateClosure.containsAttribute(
					f.getLeftHandAttributes(), rightAttr)) {
				return false;
			}
		}
		return true;
	}
	
	/*
	 * Determines if for a given FD A->B of relation R, A is a superkey or key of R.
	 */
	private boolean isAKeyOrSuperKey(final FunctionalDependency f) {
		List<Closure> allKeys = new ArrayList<>();
		allKeys.addAll(relation.getSuperKeyClosures());
		allKeys.addAll(relation.getMinimumKeyClosures());
		for (Closure c : allKeys) {
			if (c.getClosureOf().size() == f.getLeftHandAttributes().size()) {
				boolean isFullMatch = true;
				for (Attribute a : f.getLeftHandAttributes()) {
					if (!CalculateClosure.containsAttribute(
							c.getClosureOf(), a)) {
						isFullMatch = false;
						break;
					}
				}
				if (isFullMatch) {
					return true;
				}
			}
		}
		return false;
	}
	
	private void calculateThirdNormalForm() {
		if (!isSecondNormalForm) {
			isThirdNormalForm = false;
			thirdNormalFormMsg = "Input relation is not in 3NF because it is not in 2NF.";
			return;
		}
		List<FunctionalDependency> failedFDs = new ArrayList<>();
		// For each FD A ->B
		for (FunctionalDependency f : relation.getFDs()) {
			// Check if B is a subset of A (A->B is trivial)
			if (isFDTrivial(f)) {
				continue;
			}

			// Next check if A is a super key or key of the relation R
			if (isAKeyOrSuperKey(f)) {
				continue;
			}

			// Check if B is or is part of some candidate key of the relation R
			boolean isPartofKey = false;
			for (Closure c : relation.getMinimumKeyClosures()) {
				for (Attribute a : f.getRightHandAttributes()) {
					if (CalculateClosure.containsAttribute(c.getClosureOf(), a)) {
						isPartofKey = true;
						break;
					}
				}
				if (isPartofKey) {
					break;
				}
			}
			if (isPartofKey) {
				continue;
			}
			// Having not satisfied at least one of the above conditions, the
			// functional dependency is added to failed list
			failedFDs.add(f);
		}
		// Check if all FDs fit at least one of the conditions
		if (failedFDs.isEmpty()) {
			isThirdNormalForm = true;
			thirdNormalFormMsg = "Input relation is in 3NF: It is in 2NF and for each functional dependency: "
					+ "(1) The right-hand side is a subset of the left hand side, "
					+ "(2) the left-hand side is a superkey (or minimum key) of the relation, or "
					+ "(3) the right-hand side is (or is a part of) some minimum key of the relation.";
		} else {
			isThirdNormalForm = false;
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < failedFDs.size(); i++) {
				sb.append(failedFDs.get(i).getFDName());
				if (i < failedFDs.size() - 1) {
					sb.append("; ");
				}
			}
			sb.append(".");
			String failure;
			if (failedFDs.size() == 1) {
				failure = "dependency that failed is: ";
			} else {
				failure = "dependencies that failed are: ";
			}
			thirdNormalFormMsg = "Input relation is not in 3NF: it is in 2NF but "
					+ "not all functional dependencies satisfy at least one of the following conditions: "
					+ "(1) The right-hand side is a subset of the left hand side, "
					+ "(2) the left-hand side is a superkey (or minimum key) of the relation, or "
					+ "(3) the right-hand side is (or is a part of) some minimum key of the relation. "
					+ "The functional " + failure + sb.toString();
		}
	}

	private void calculateBCNF() {
		if (!isThirdNormalForm) {
			isBCNF = false;
			BCNFMsg = "Input relation is not in BCNF because it is not in 3NF.";
			return;
		}
		List<FunctionalDependency> failedFDs = new ArrayList<>();
		// For each FD A ->B
		for (FunctionalDependency f : relation.getFDs()) {
			// Check if B is a subset of A (A->B is trivial)
			if (isFDTrivial(f)) {
				continue;
			}
			// Check if A is a superkey of B
			if (isAKeyOrSuperKey(f)) {
				continue;
			}
			// Having not satisfied at least one of the previous conditions, the FD violates BCNF
			failedFDs.add(f);
		}
		
		if (failedFDs.isEmpty()) {
			isBCNF = true;
			BCNFMsg = "Input relation is in BCNF: it is in 3NF and for each functional dependency: "
					+ "(1) The right-hand side is a subset of the left hand side, or "
					+ "(2) the left-hand side is a superkey (or minimum key) of the relation.";
		} else {
			isBCNF = false;
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < failedFDs.size(); i++) {
				sb.append(failedFDs.get(i).getFDName());
				if (i < failedFDs.size() - 1) {
					sb.append("; ");
				}
			}
			sb.append(".");
			String failure;
			if (failedFDs.size() == 1) {
				failure = "dependency that failed is: ";
			} else {
				failure = "dependencies that failed are: ";
			}
			BCNFMsg = "Input relation is not in BCNF: it is in 3NF but "
					+ "not all functional dependencies satisfy at least one of the following conditions: "
					+ "(1) The right-hand side is a subset of the left hand side, or "
					+ "(2) the left-hand side is a superkey (or minimum key) of the relation. "
					+ "The functional " + failure + sb.toString();
		}
	}
	
	protected String getFirstNormalFormMsg() {
		return firstNormalFormMsg;
	}

	protected String getSecondNormalFormMsg() {
		return secondNormalFormMsg;
	}
	
	protected String getThirdNormalFormMsg() {
		return thirdNormalFormMsg;
	}
	
	protected String getBCNFMsg() {
		return BCNFMsg;
	}
	
	protected boolean isIn3NF() {
		return isThirdNormalForm;
	}
}
