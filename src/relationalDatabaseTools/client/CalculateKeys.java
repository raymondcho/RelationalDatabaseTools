package relationalDatabaseTools.client;

import java.util.List;

/**
 * Contains static methods used to calculate a relation's minimum candidate keys and superkeys.
 * @author Raymond Cho
 *
 */
public class CalculateKeys {
	
	public static void calculateKeys(final Relation relation) {
		List<Closure> closures = relation.getClosures();
		if (closures.isEmpty()) {
			return;
		}
		boolean foundMinimum = false;
		int minimumKeySize = relation.getAttributes().size();
		for (Closure closure : closures) {
			List<Attribute> left = closure.getClosureOf();
			List<Attribute> right = closure.getClosure();
			if (right.size() == relation.getAttributes().size()) {
				if (!foundMinimum) {
					foundMinimum = true;
					minimumKeySize = left.size();
				}
				if (left.size() == minimumKeySize) {
					relation.addMinimumKeyClosure(closure);
				} else {
					boolean addedtoSuperKeyClosure = false;
					for (Closure minimumClosure : relation.getMinimumKeyClosures()) {
						boolean earlyTerminateLocal = false;
						for (Attribute minAttr : minimumClosure.getClosureOf()) {
							if (!RDTUtils.attributeListContainsAttribute(left, minAttr)) {
								earlyTerminateLocal = true;
								break;
							}
						}
						if (!earlyTerminateLocal) {
							relation.addSuperKeyClosure(closure);
							addedtoSuperKeyClosure = true;
							break;
						}
					}
					if (!addedtoSuperKeyClosure) {
						relation.addMinimumKeyClosure(closure);
					}
				}
			}
		}
		calculateNonPrimeAttributes(relation);
		calculatePrimeAttributes(relation);
	}
	
	public static void calculateNonPrimeAttributes(final Relation relation) {
		List<Closure> minimumKeys = relation.getMinimumKeyClosures();
		List<Attribute> allAttributes = relation.getAttributes();
		for (Attribute a : allAttributes) {
			if (!RDTUtils.closureListContainsAttribute(minimumKeys, a)) {
				relation.addNonPrimeAttribute(a);
			}
		}
	}
	
	public static void calculatePrimeAttributes(final Relation relation) {
		List<Attribute> allAttributes = relation.getAttributes();
		List<Attribute> nonPrimeAttributes = relation.getNonPrimeAttributes();
		for (Attribute a : allAttributes) {
			if (!RDTUtils.attributeListContainsAttribute(nonPrimeAttributes, a)) {
				relation.addPrimeAttribute(a);
			}
		}
	}
}
