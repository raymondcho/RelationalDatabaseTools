package relationalDatabaseTools.client;

import java.util.List;

public class TestMain {
	
	public static void main(String[] args) {
		String attributes = "R(NAME, STREET, CITY, TITLE, YEAR)";
		String functionalDependencies = "";
		String multivaluedDependencies = "NAME->STREET,CITY";
//		String attributes = "R(TITLE, YEAR, STUDIONAME, PRESIDENT, PRESADDR)";
//		String functionalDependencies = "TITLE, YEAR -> STUDIONAME; STUDIONAME -> PRESIDENT; PRESIDENT -> PRESADDR";
		Relation relation = new Relation(attributes);
		relation.addFunctionalDependencies(functionalDependencies);
		relation.addMultivaluedDependencies(multivaluedDependencies);
		if (!relation.hasPassedIntegrityChecks()) {
			System.out.println("Failed integrity check.");
			System.out.println(relation.getIntegrityCheckErrorMsg());
		}
		System.out.println("Input relation: " + relation.printRelation());
		System.out.println("-------------");
		CalculateClosure.improvedCalculateClosures(relation);
		for (Closure c : relation.getClosures()) {
			System.out.println(c.printCompleteClosure());
		}
		System.out.println("-------------");
		System.out.println("Minimum key closures: ");
		for (Closure c : relation.getMinimumKeyClosures()) {
			System.out.println(c.printLeftSideAttributes());
		}
		System.out.println("Superkey closures: ");
		for (Closure c : relation.getSuperKeyClosures()) {
			System.out.println(c.printLeftSideAttributes());
		}
		System.out.println("-------------");
		MinimalFDCover.determineMinimalCover(relation);
		System.out.println("Minimal cover: ");
		for (FunctionalDependency fd : relation.getMinimalCover()) {
			System.out.println(fd.getFDName());
		}
		System.out.println("-------------");
		relation.determineNormalForms();
		DetermineNormalForms normalForms = relation.getNormalFormsResults();
		System.out.println(normalForms.getSecondNormalFormMsg());
		System.out.println(normalForms.getThirdNormalFormMsg());
		System.out.println(normalForms.getBCNFMsg());
		System.out.println(normalForms.getFourthNormalFormMsg());
		Calculate3NFDecomposition threeNF = new Calculate3NFDecomposition(relation);
		CalculateBCNFDecomposition bcnf = new CalculateBCNFDecomposition(threeNF);
		bcnf.decompose();
		System.out.println(bcnf.getOutputMsg());
		System.out.println("Results with possible duplicates: ");
		for (Relation r : bcnf.getResultWithDuplicates()) {
			System.out.println(r.printRelation());
		}
		System.out.println("Final results:");
		List<Relation> outputBCNFRelations = bcnf.getOutputRelations();
		for (Relation r : outputBCNFRelations) {
			System.out.println(r.printRelation());
		}
	}
	
}
