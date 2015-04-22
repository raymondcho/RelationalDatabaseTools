package relationalDatabaseTools.client;

import java.util.ArrayList;
import java.util.List;

public class CalculateFDs {
	
	public static void calculateDerivedFDs(final Relation relation) {
		if (relation.getClosures().isEmpty()) {
			CalculateClosure.improvedCalculateClosures(relation);
		}
		for (Closure c : relation.getClosures()) {
			List<Attribute> rightSide = new ArrayList<>();
			for (Attribute a : c.getClosure()) {
				if (!CalculateClosure.containsAttribute(c.getClosureOf(), a)){
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
