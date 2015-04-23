package relationalDatabaseTools.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains static utility methods
 * 
 * @author Raymond Cho
 * 
 */
public class RDTUtils {

	/**
	 * @param attributeList
	 * @param attribute
	 * @return True if the attribute is contained in the attributeList and false
	 *         otherwise.
	 */
	protected static boolean attributeListContainsAttribute(final List<Attribute> attributeList,
			final Attribute attribute) {
		for (Attribute a : attributeList) {
			if (a.getName().equals(attribute.getName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @param firstAttributeList
	 * @param secondAttributeList
	 * @return True if second list's attributes are all found in the first list
	 *         and false otherwise.
	 */
	protected static boolean isAttributeListSubsetOfOtherAttributeList(final List<Attribute> firstAttributeList,
			final List<Attribute> secondAttributeList) {
		if (firstAttributeList.size() < secondAttributeList.size()) {
			return false;
		}
		for (Attribute a : secondAttributeList) {
			if (!attributeListContainsAttribute(firstAttributeList, a)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param closureList
	 * @param attribute
	 * @return True if the attribute is contained in a closure that is in the
	 *         closureList and false otherwise.
	 */
	protected static boolean closureListContainsAttribute(final List<Closure> closureList, final Attribute attribute) {
		for (Closure c : closureList) {
			for (Attribute a : c.getClosureOf()) {
				if (a.getName().equals(attribute.getName())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 
	 * @param firstClosure
	 * @param secondClosure
	 * @return True if second closure is a proper subset of the first closure
	 *         and false otherwise.
	 */
	protected static boolean isClosureProperSubsetOfOtherClosure(final Closure firstClosure, final Closure secondClosure) {
		if (firstClosure.getClosureOf().size() <= secondClosure.getClosureOf().size()) {
			return false;
		}
		for (Attribute a : secondClosure.getClosureOf()) {
			if (!attributeListContainsAttribute(firstClosure.getClosureOf(), a)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param fdList
	 * @param decomposedRAttrs
	 * @return List of all functional dependencies from input FD list that have
	 *         attributes that match the input list of attributes.
	 */
	protected static List<FunctionalDependency> fetchFDsOfDecomposedR(final List<FunctionalDependency> fdList,
			final List<Attribute> decomposedRAttrs) {
		List<FunctionalDependency> result = new ArrayList<>();
		if (fdList == null || decomposedRAttrs == null || fdList.isEmpty() || decomposedRAttrs.isEmpty()) {
			return result;
		}
		for (FunctionalDependency f : fdList) {
			List<Attribute> fdAttrs = new ArrayList<>();
			for (Attribute a : f.getLeftHandAttributes()) {
				if (!attributeListContainsAttribute(fdAttrs, a)) {
					fdAttrs.add(a);
				}
			}
			for (Attribute a : f.getRightHandAttributes()) {
				if (!attributeListContainsAttribute(fdAttrs, a)) {
					fdAttrs.add(a);
				}
			}
			if (isAttributeListSubsetOfOtherAttributeList(decomposedRAttrs, fdAttrs)) {
				result.add(f);
			}
		}
		return result;
	}
}
