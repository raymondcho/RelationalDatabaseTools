package relationalDatabaseTools.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to decompose a relation not in Boyce-Codd normal form into a collection of relations that are in Boyce-Codd normal form.
 * @author Raymond Cho
 *
 */
public class CalculateBCNFDecomposition extends CalculateDecomposition {

	public CalculateBCNFDecomposition(Relation inputRelation) {
		super(inputRelation);
	}

	@Override
	protected void decompose() {
		// TODO Auto-generated method stub
		
	}

}
