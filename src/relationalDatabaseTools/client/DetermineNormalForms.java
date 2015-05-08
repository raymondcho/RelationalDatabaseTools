package relationalDatabaseTools.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks input relation for compliance with normal forms.
 * 
 * @author Raymond Cho
 * 
 */
public class DetermineNormalForms {
	private final Relation relation;
	public boolean hasDeterminedNormalForms;
	private boolean isFirstNormalForm;
	private String firstNormalFormMsg;
	private boolean isSecondNormalForm;
	private String secondNormalFormMsg;
	private boolean isThirdNormalForm;
	private String thirdNormalFormMsg;
	private boolean isBCNF;
	private String BCNFMsg;
	private boolean isFourthNormalForm;
	private String fourthNormalFormMsg;
	private List<FunctionalDependency> bcnfViolatingFDs;

	public DetermineNormalForms(final Relation relation) {
		this.relation = relation;
		hasDeterminedNormalForms = false;
		bcnfViolatingFDs = null;
	}

	public void calculateNormalForms() {
		calculateFirstNormalForm();
		calculateSecondNormalForm();
		calculateThirdNormalForm();
		calculateBCNF();
		calculateFourthNormalForm();
		hasDeterminedNormalForms = true;
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
					if (!RDTUtils.attributeListContainsAttribute(primeAttributes, ab)) {
						nonPrimes.add(ab);
					}
				}
				for (Attribute ac : nonPrimes) {
					for (Closure c : relation.getClosures()) {
						if (c.getClosureOf().size() >= minClosure.getClosureOf().size()) {
							break;
						}
						if (RDTUtils.isClosureProperSubsetOfOtherClosure(minClosure, c)) {
							if (RDTUtils.attributeListContainsAttribute(c.getClosure(), ac)
									&& !RDTUtils.attributeListContainsAttribute(c.getClosureOf(), ac)) {
								if (!RDTUtils.attributeListContainsAttribute(failedAttrs, ac)) {
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
						+ "The following non-prime "
						+ attribute + " the condition: " + sb.toString();
			}
		}
	}

	/**
	 * 
	 * @param functionalDependency
	 * @return True if input functional dependency is trivial: all of its
	 *         right-hand side attributes are also in its left-hand side.
	 */
	private boolean isTrivialFD(final FunctionalDependency f) {
		for (Attribute rightAttr : f.getRightHandAttributes()) {
			if (!RDTUtils.attributeListContainsAttribute(f.getLeftHandAttributes(), rightAttr)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param dependency
	 * @return True if input dependency A->B of relation R, A is a
	 *         superkey or key of R.
	 */
	@SuppressWarnings("rawtypes")
	private boolean isAKeyOrSuperKey(final Dependency dependency) {
		List<Closure> allKeys = new ArrayList<>();
		allKeys.addAll(relation.getSuperKeyClosures());
		allKeys.addAll(relation.getMinimumKeyClosures());
		for (Closure c : allKeys) {
			if (c.getClosureOf().size() == dependency.getLeftHandAttributes().size()) {
				boolean isFullMatch = true;
				List rawAttrs = dependency.getLeftHandAttributes();
				List<Attribute> castAttrs = new ArrayList<>();
				for (Object o : rawAttrs) {
					Attribute a = (Attribute) o;
					castAttrs.add(a);
				}
				for (Attribute a : castAttrs) {
					if (!RDTUtils.attributeListContainsAttribute(c.getClosureOf(), a)) {
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
	
	private boolean isTrivialMultivaluedDependency(final MultivaluedDependency m) {
		// First check if all attributes are found in the MVD (regardless of side). If so, then the MVD is trivial.
		List<Attribute> mvdAttrs = new ArrayList<>();
		for (Attribute a : m.getLeftHandAttributes()) {
			if (!RDTUtils.attributeListContainsAttribute(mvdAttrs, a)) {
				mvdAttrs.add(a);
			}
		}
		for (Attribute a : m.getRightHandAttributes()) {
			if (!RDTUtils.attributeListContainsAttribute(mvdAttrs, a)) {
				mvdAttrs.add(a);
			}
		}
		if (mvdAttrs.size() == relation.getAttributes().size()) {
			return true;
		}
		// Next check if all attributes on right side are also found in left side
		for (Attribute rightAttr : m.getRightHandAttributes()) {
			if (!RDTUtils.attributeListContainsAttribute(m.getLeftHandAttributes(), rightAttr)) {
				return false;
			}
		}
		return true;
	}

	private void calculateThirdNormalForm() {
		List<FunctionalDependency> failedFDs = new ArrayList<>();
		// For each FD A ->B
		for (FunctionalDependency f : relation.getFDs()) {
			// Check if B is a subset of A (A->B is trivial)
			if (isTrivialFD(f)) {
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
					if (RDTUtils.attributeListContainsAttribute(c.getClosureOf(), a)) {
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
		if (failedFDs.isEmpty() && isSecondNormalForm) {
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
			String twoNFStatus;
			if (isSecondNormalForm) {
				twoNFStatus = "it is in 2NF but ";
			} else {
				twoNFStatus = "it is not in 2NF and ";
			}
			thirdNormalFormMsg = "Input relation is not in 3NF: " + twoNFStatus 
					+ "not all functional dependencies satisfy at least one of the following conditions: "
					+ "(1) The right-hand side is a subset of the left hand side, "
					+ "(2) the left-hand side is a superkey (or minimum key) of the relation, or "
					+ "(3) the right-hand side is (or is a part of) some minimum key of the relation. "
					+ "The functional " + failure + sb.toString();
		}
	}

	private void calculateBCNF() {
		List<FunctionalDependency> failedFDs = new ArrayList<>();
		// For each FD A ->B
		for (FunctionalDependency f : relation.getFDs()) {
			// Check if B is a subset of A (A->B is trivial)
			if (isTrivialFD(f)) {
				continue;
			}
			// Check if A is a superkey of input relation
			if (isAKeyOrSuperKey(f)) {
				continue;
			}
			// Having not satisfied at least one of the previous conditions, the
			// FD violates BCNF
			failedFDs.add(f);
		}

		if (failedFDs.isEmpty() && isThirdNormalForm) {
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
			String threeNFStatus;
			if (isThirdNormalForm) {
				threeNFStatus = "it is in 3NF but ";
			} else {
				threeNFStatus = "it is not in 3NF and ";
			}
			BCNFMsg = "Input relation is not in BCNF: " + threeNFStatus
					+ "not all functional dependencies satisfy at least one of the following conditions: "
					+ "(1) The right-hand side is a subset of the left hand side, or "
					+ "(2) the left-hand side is a superkey (or minimum key) of the relation. " + "The functional "
					+ failure + sb.toString();
		}
	}

	private void calculateFourthNormalForm() {
		fourthNormalFormMsg = "";
		List<MultivaluedDependency> failedMVDs = new ArrayList<>();
		// Promote all FDs into MVDs
		List<MultivaluedDependency> combinedMVDs = relation.getMVDs();
		for (FunctionalDependency fd : relation.getFDs()) {
			MultivaluedDependency mvd = new MultivaluedDependency(fd.getLeftHandAttributes(), fd.getRightHandAttributes(), relation);
			boolean duplicateCheck = true;
			for (MultivaluedDependency m : combinedMVDs) {
				if (m.getName().equals(mvd.getName())) {
					duplicateCheck = false;
					break;
				}
			}
			if (duplicateCheck) {
				combinedMVDs.add(mvd);
			}
		}
		// For each MVD A -->-> B
		for (MultivaluedDependency m : combinedMVDs) {
			System.out.println(m.getName());
			// Check if the MVD is trivial
			if (isTrivialMultivaluedDependency(m)) {
				System.out.println("Trivial");
				continue;
			}
			// Check if the A is a superkey of relation R
			if (isAKeyOrSuperKey(m)) {
				System.out.println("Key");
				continue;
			}
			// Having not satisfied at least one of the previous conditions, the
			// MVD violates 4NF
			failedMVDs.add(m);
		}
		if (failedMVDs.isEmpty()) {
			isFourthNormalForm = true;
			fourthNormalFormMsg += "Input relation is in 4NF: it is in BCNF and for each of its nontrivial multivalued dependencies: "
				+ "the left-hand side is a superkey (or minimum key) of the relation. "
				+ "A multivalued dependency is trivial if either (1) the right-hand side is a subset of the left-hand side, or "
				+ "(2) the multivalued dependency contains all attributes of the input relation.";
		} else {
			isFourthNormalForm = false;
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < failedMVDs.size(); i++) {
				sb.append(failedMVDs.get(i).getName());
				if (i < failedMVDs.size() - 1) {
					sb.append("; ");
				}
			}
			sb.append(".");
			String failure;
			if (failedMVDs.size() == 1) {
				failure = "dependency that failed is: ";
			} else {
				failure = "dependencies that failed are: ";
			}
			String bcnfStatus;
			if (isBCNF) {
				bcnfStatus = "it is in BCNF but ";
			} else {
				bcnfStatus = "it is not in BCNF and ";
			}
			fourthNormalFormMsg += "Input relation is not in 4NF: " + bcnfStatus
					+ "not all nontrivial multivalued dependencies satisfied the 4NF condition that "
					+ "the left-hand side is a superkey (or minimum key) of the relation. "
					+ "A multivalued dependency is trivial if either (1) the right-hand side is a subset of the left-hand side, or "
					+ "(2) the multivalued dependency contains all attributes of the input relation. "
					+ "The multivalued "
					+ failure + sb.toString();
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
	
	protected String getFourthNormalFormMsg() {
		return fourthNormalFormMsg;
	}

	protected boolean isIn3NF() {
		return isThirdNormalForm;
	}
	
	protected boolean isInBCNF() {
		return isBCNF;
	}
	
	protected boolean isIn4NF() {
		return isFourthNormalForm;
	}
	
	protected List<FunctionalDependency> getBCNFViolatingFDs() {
		if (bcnfViolatingFDs == null) {
			bcnfViolatingFDs = new ArrayList<>();
			for (FunctionalDependency f : relation.getFDs()) {
				// Check if B is a subset of A (A->B is trivial)
				if (isTrivialFD(f)) {
					continue;
				}
				// Check if A is a superkey of input relation
				if (isAKeyOrSuperKey(f)) {
					continue;
				}
				// Having not satisfied at least one of the previous conditions, the
				// FD violates BCNF
				bcnfViolatingFDs.add(f);
			}
		}
		return bcnfViolatingFDs;
	}
}
