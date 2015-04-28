package relationalDatabaseTools.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a relation.
 * @author Raymond Cho
 *
 */
public class Relation {

	private static String EMPTY = "";
	
	private final String name;
	private final List<Attribute> attributes;
	private final List<Attribute> primeAttributes;
	private final List<Attribute> nonPrimeAttributes;
	private boolean passedIntegrityChecks;
	private String integrityCheckErrorMsg;
	private final List<FunctionalDependency> fds;
	private final List<FunctionalDependency> workingFDs;
	private final List<FunctionalDependency> derivedFDs;
	private final List<FunctionalDependency> minimalCover;
	private final List<Closure> closures;
	private final List<Closure> minimumKeys;
	private final List<Closure> superKeys;
	private DetermineNormalForms normalFormResults;
	
	public Relation(final String input) {
		this.name = parseName(input);
		passedIntegrityChecks = true;
		integrityCheckErrorMsg = "";
		this.attributes = parseAttributes(input);
		this.primeAttributes = new ArrayList<>();
		this.nonPrimeAttributes = new ArrayList<>();
		this.fds = new ArrayList<>();
		this.workingFDs = new ArrayList<>();
		this.derivedFDs = new ArrayList<>();
		this.minimalCover = new ArrayList<>();
		this.closures = new ArrayList<>();
		this.minimumKeys = new ArrayList<>();
		this.superKeys = new ArrayList<>();
		this.normalFormResults = new DetermineNormalForms(this);
	}
	
	public Relation(final String name, final List<Attribute> attributes, final List<FunctionalDependency> fds) {
		this.name = name;
		passedIntegrityChecks = true;
		integrityCheckErrorMsg = "";
		this.attributes = attributes;
		this.primeAttributes = new ArrayList<>();
		this.nonPrimeAttributes = new ArrayList<>();
		this.fds = fds;
		this.workingFDs = new ArrayList<>();
		this.derivedFDs = new ArrayList<>();
		this.minimalCover = new ArrayList<>();
		this.closures = new ArrayList<>();
		this.minimumKeys = new ArrayList<>();
		this.superKeys = new ArrayList<>();
		this.normalFormResults = new DetermineNormalForms(this);
	}
	
	public String getName() {
		return name;
	}
	
	public List<Attribute> getAttributes() {
		return attributes;
	}
	
	public static String parseName(final String input) {
		if (isNullOrEmpty(input)) {
			return EMPTY;
		}
		for (int i = 1; i < input.length(); i++) {
			if (input.charAt(i) == '(') {
				return input.substring(0, i);
			}
		}
		return EMPTY;
	}
	
	public List<Attribute> parseAttributes(final String input) {
		List<Attribute> result = new ArrayList<>();
		if (!schemaContainsParenthesisPair(input)) {
			return result;
		}
		int start = input.indexOf('(') + 1;
		int end = input.indexOf(')');
		String attributePortion = input.substring(start, end);
		String[] attributes = attributePortion.split(",");
		for (String attribute : attributes) {
			if (!isNullOrEmpty(attribute)) {
				Attribute a = new Attribute(attribute.trim());
				for (Attribute b : result) {
					if (b.getName().equals(a.getName())) {
						integrityCheckErrorMsg = "Duplicate attribute encountered: " + attribute.trim();
						passedIntegrityChecks = false;
						return result;
					}
				}
				result.add(a);
			}
		}
		return result;
	}
	
	public void addFunctionalDependencies(final String input) {
		String trimmedInput = input.replaceAll("\\s","");
		if (trimmedInput.isEmpty()) {
			return;
		}
		String[] FDs;
		if (trimmedInput.contains(";")) {
			FDs = trimmedInput.split(";");
		} else {
			FDs = new String[1];
			FDs[0] = trimmedInput;
		}
		for (String prefunctional : FDs) {
			FunctionalDependency fd = new FunctionalDependency(prefunctional, this);
			if (fd.isProperFD) {
				boolean duplicateCheck = false;
				for (FunctionalDependency f : fds) {
					if (f.getFDName().equals(fd.getFDName())) {
						integrityCheckErrorMsg = "Duplicate functional dependency encountered: " + fd.getFDName();
						passedIntegrityChecks = false;
						return;
					}
				}
				if (!duplicateCheck) {
					fds.add(fd);
				}
			}
		}
		// Split FDs that have more than one attribute on right-hand side into single right-hand side FDs.
		for (FunctionalDependency f : fds) {
			if (f.getRightHandAttributes().size() == 1) {
				workingFDs.add(f);
			} else {
				for (Attribute a : f.getRightHandAttributes()) {
					List<Attribute> rightSplitted = new ArrayList<>();
					rightSplitted.add(a);
					FunctionalDependency splitted = new FunctionalDependency(f.getLeftHandAttributes(), rightSplitted, this);
					workingFDs.add(splitted);
				}
			}
		}
		Collections.sort(fds);
		sortFDs();
	}
	
	protected void sortFDs() {
		Collections.sort(workingFDs);
		Collections.sort(derivedFDs);
	}
	
	protected String printRelation() {
		StringBuilder sb = new StringBuilder();
		sb.append(name);
		sb.append("(");
		for (int i = 0; i < attributes.size(); i++) {
			sb.append(attributes.get(i).getName());
			if (i < attributes.size() - 1) {
				sb.append(", ");
			}
		}
		sb.append(") having FD(s): ");
		for (int i = 0; i < fds.size(); i++) {
			sb.append(fds.get(i).getFDName());
			if (i < fds.size() - 1) {
				sb.append("; ");
			}
		}
		if (fds.isEmpty()) {
			sb.append("(none)");
		}
		sb.append(".");
		return sb.toString();
	}
	
	protected void addDerivedFunctionalDependency(final FunctionalDependency f) {
		boolean duplicateCheck = true;
		for (FunctionalDependency fd : workingFDs) {
			if (fd.getFDName().equals(f.getFDName())) {
				duplicateCheck = false;
				break;
			}
		}
		if (duplicateCheck) {
			workingFDs.add(f);
			derivedFDs.add(f);
		}
		return;
	}
	
	protected void addMinimalCoverFD(final FunctionalDependency f) {
		minimalCover.add(f);
	}
	
	protected void sortMinimalCover() {
		Collections.sort(minimalCover);
	}
	
	public List<FunctionalDependency> getInputFDs() {
		return fds;
	}
	
	public List<FunctionalDependency> getFDs() {
		return minimalCover;
	}
	
	public List<FunctionalDependency> getDerivedFDs() {
		return derivedFDs;
	}
	
	public List<FunctionalDependency> getMinimalCover() {
		return minimalCover;
	}
	
	protected Attribute getAttribute(String name) {
		for (Attribute a : attributes) {
			if (a.getName().equals(name)) {
				return a;
			}
		}
		return null;
	}
	
	public boolean hasPassedIntegrityChecks() {
		return passedIntegrityChecks;
	}
	
	protected void setPassedIntegrityChecks(final boolean check) {
		this.passedIntegrityChecks = check;
	}
	
	public String getIntegrityCheckErrorMsg() {
		return integrityCheckErrorMsg;
	}
	
	protected void setIntegrityCheckErrorMsg(final String msg) {
		this.integrityCheckErrorMsg = msg;
	}
	
	protected List<Closure> getClosures() {
		return closures;
	}
	
	protected void addClosure(final Closure closure) {
		closures.add(closure);
	}
	
	protected void sortClosures() {
		Collections.sort(closures);
	}
	
	protected void addMinimumKeyClosure(final Closure closure) {
		minimumKeys.add(closure);
	}
	
	protected List<Closure> getMinimumKeyClosures() {
		return minimumKeys;
	}
	
	protected void addSuperKeyClosure(final Closure closure) {
		superKeys.add(closure);
	}
	
	
	protected List<Closure> getSuperKeyClosures() {
		return superKeys;
	}
	
	protected void addPrimeAttribute(final Attribute attribute) {
		primeAttributes.add(attribute);
	}
	
	protected List<Attribute> getPrimeAttributes() {
		return primeAttributes;
	}
	
	protected void addNonPrimeAttribute(final Attribute attribute) {
		nonPrimeAttributes.add(attribute);
	}
	
	protected List<Attribute> getNonPrimeAttributes() {
		return nonPrimeAttributes;
	}
	
	protected void determineNormalForms() {
		normalFormResults.calculateNormalForms();
	}
	
	protected DetermineNormalForms getNormalFormsResults() {
		return normalFormResults;
	}
	
	public static boolean isNullOrEmpty(final String s) {
		if (s == null || s.isEmpty()) {
			return true;
		}
		return false;
	}
	
	public static boolean schemaContainsParenthesisPair(final String s) {
		if (!isNullOrEmpty(s)) {
			int openP = 0;
			int closeP = 0;
			for (int i = 0; i < s.length(); i++) {
				if (s.charAt(i) == '(') {
					if (openP == 0 && closeP == 0) {
						openP++;
					} else {
						return false;
					}
				}
				if (s.charAt(i) == ')') {
					if (openP == 1 && closeP == 0) {
						closeP++;
					} else {
						return false;
					}
				}
			}
			if (openP == 1 && closeP == 1) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean schemaContainsSafeChars(final String input) {
		// Acceptable characters are all upper-case letters, commas, and parenthesis.
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if ((c >= 'A' && c <= 'Z') || c == ',' || c == '(' || c == ')' || c == ' ') {
				continue;
			} else {
				return false;
			}
		}
		return true;
	}
	
	public static boolean functionalContainsSafeChars(final String input) {
		// Acceptable characters are all upper-case letters, commas, semi-colons, hyphens, and greater-than.
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if ((c >= 'A' && c <= 'Z') || c == ',' || c == ';' || c == '-' || c == '>' || c == ' ') {
				continue;
			} else {
				return false;
			}
		}
		return true;
	}
	
	public static boolean functionalContainsArrows(final String input) {
		// Checks if all hyphens are immediately followed by greater-than and none are unmatched.
		int arrowCount = 0;
		boolean matchedHyphen = false;
		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (matchedHyphen) {
				if (c == '>') {
					arrowCount++;
					matchedHyphen = false;
					continue;
				}
				// If get here, means preceding char was hyphen but current char is not >
				return false;
			}
			if (c == '-') {
				if (matchedHyphen) {
					return false;
				}
				matchedHyphen = true;
				continue;
			}
		}
		if (arrowCount < 1 || matchedHyphen) {
			return false;
		}
		return true;
	}
	
	public static boolean functionalContainsAtLeastOneDependency(final String input) {
		if (functionalContainsArrows(input)) {
			String[] splitted = input.replaceAll("\\s","").split("->");
			try {
				char firstLeft = splitted[0].charAt(0);
				char firstRight = splitted[1].charAt(0);
				if (firstLeft >= 'A' && firstLeft <= 'Z' && firstRight >= 'A' && firstRight <= 'Z') {
					return true;
				}
				return false;
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}
	
}
