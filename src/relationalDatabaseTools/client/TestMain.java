package relationalDatabaseTools.client;

import java.util.List;

public class TestMain {
	
	public static void main(String[] args) {
		String attributes = "R(A,B,C,D,E,F,G)";
		String functionalDependencies = "B->D; D,G->C; B,D->E; A,G->B;A,D,G->B;A,D,G->C";
//		String attributes = "R(TITLE, YEAR, STUDIONAME, PRESIDENT, PRESADDR)";
//		String functionalDependencies = "TITLE, YEAR -> STUDIONAME; STUDIONAME -> PRESIDENT; PRESIDENT -> PRESADDR";
		Relation relation = new Relation(attributes);
		relation.addFunctionalDependencies(functionalDependencies);
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
//		System.out.println("-------------");
//		relation.determineNormalForms();
//		DetermineNormalForms normalForms = relation.getNormalFormsResults();
//		System.out.println(normalForms.getSecondNormalFormMsg());
//		System.out.println(normalForms.getThirdNormalFormMsg());
//		System.out.println(normalForms.getBCNFMsg());
//		CalculateBCNFDecomposition bcnf = new CalculateBCNFDecomposition(relation);
//		bcnf.decompose();
//		System.out.println(bcnf.getOutputMsg());
//		List<Relation> outputBCNFRelations = bcnf.getOutputRelations();
//		for (Relation r : outputBCNFRelations) {
//			System.out.println(r.printRelation());
//		}
	}
	
}
