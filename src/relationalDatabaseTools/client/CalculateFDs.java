package relationalDatabaseTools.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains static methods for deriving new functional dependencies from a
 * relation's input functional dependencies.
 * 
 * @author Raymond Cho
 * 
 */
public class CalculateFDs {

	public static void calculateDerivedFDs(final Relation relation) {
		if (relation.getClosures().isEmpty()) {
			CalculateClosure.improvedCalculateClosures(relation);
		}
		for (Closure c : relation.getClosures()) {
			List<Attribute> rightSide = new ArrayList<>();
			for (Attribute a : c.getClosure()) {
				if (!RDTUtils.attributeListContainsAttribute(c.getClosureOf(), a)) {
					rightSide.add(a);
				}
			}
			if (!rightSide.isEmpty()) {
				for (Attribute b : rightSide) {
					List<Attribute> rightDerived = new ArrayList<>();
					rightDerived.add(b);
					FunctionalDependency derived = new FunctionalDependency(c.getClosureOf(), rightDerived, relation);
					relation.addDerivedFunctionalDependency(derived);
				}
			}
		}
		relation.sortFDs();
		return;
	}
}
