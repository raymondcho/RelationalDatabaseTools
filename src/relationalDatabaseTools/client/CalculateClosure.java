package relationalDatabaseTools.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains static methods for calculating the closure of attributes based
 * on a list of given functional dependencies.
 * @author Raymond Cho
 * 
 */
public class CalculateClosure {

	public static Closure calculateClosureOf(final List<Attribute> closureAttributes,
			final List<FunctionalDependency> givenFDs) {
		if (closureAttributes == null || givenFDs == null || closureAttributes.isEmpty()) {
			return null;
		}
		List<Attribute> leftSideClosure = new ArrayList<>();
		List<Attribute> rightSideClosure = new ArrayList<>();
		for (Attribute a : closureAttributes) {
			if (!RDTUtils.attributeListContainsAttribute(leftSideClosure, a)) {
				leftSideClosure.add(a);
			}
			if (!RDTUtils.attributeListContainsAttribute(rightSideClosure, a)) {
				rightSideClosure.add(a);
			}
		}
		int closureSize = rightSideClosure.size();
		List<FunctionalDependency> addedFDs = new ArrayList<>();
		while (true) {
			for (FunctionalDependency f : givenFDs) {
				if (!addedFDs.contains(f)) {
					boolean containsAll = true;
					for (Attribute leftAttr : f.getLeftHandAttributes()) {
						if (!RDTUtils.attributeListContainsAttribute(rightSideClosure, leftAttr)) {
							containsAll = false;
							break;
						}
					}
					if (containsAll) {
						for (Attribute rightAttr : f.getRightHandAttributes()) {
							if (!RDTUtils.attributeListContainsAttribute(rightSideClosure, rightAttr)) {
								rightSideClosure.add(rightAttr);
							}
						}
						addedFDs.add(f);
					}
				}
			}
			if (rightSideClosure.size() > closureSize) {
				closureSize = rightSideClosure.size();
				continue;
			} else {
				break;
			}
		}
		Collections.sort(leftSideClosure);
		Collections.sort(rightSideClosure);

		return new Closure(leftSideClosure, rightSideClosure);
	}

	public static void improvedCalculateClosures(final Relation relation) {
		List<Attribute> relationAttributes = relation.getAttributes();
		BinaryCounter counter = new BinaryCounter(relationAttributes.size());
		while (!counter.hasReachedMax()) {
			boolean[] selectAttributes = counter.getCounter();
			List<Attribute> selectedAttributes = new ArrayList<>();
			for (int i = 0; i < selectAttributes.length; i++) {
				if (selectAttributes[i]) {
					selectedAttributes.add(relationAttributes.get(i));
				}
			}
			Closure c = calculateClosureOf(selectedAttributes, relation.getInputFDs());
			if (c != null) {
				relation.addClosure(c);
			}
			counter.incrementCounter();
		}
		relation.sortClosures();
		CalculateKeys.calculateKeys(relation);
	}

	protected static String printClosureOf(final Closure closure) {
		StringBuilder sb = new StringBuilder();
		sb.append("{ ");
		for (int i = 0; i < closure.getClosureOf().size(); i++) {
			sb.append(closure.getClosureOf().get(i));
			if (i < closure.getClosureOf().size() - 1) {
				sb.append(", ");
			}
		}
		sb.append(" }");
		return sb.toString();
	}
}
