package relationalDatabaseTools.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Superclass for subclasses that implement a particular kind of relation decomposition (i.e. 3NF or BCNF)
 * @author Raymond Cho
 *
 */
public abstract class CalculateDecomposition {
	private final Relation inputRelation;
	private final List<Relation> outputRelations;
	private boolean outputMsgFlag;
	private String outputMsg;
	
	public CalculateDecomposition(final Relation inputRelation) {
		this.inputRelation = inputRelation;
		this.outputRelations = new ArrayList<>();
		outputMsgFlag = false;
		outputMsg = "";
	}
	
	protected Relation getInputRelation() {
		return inputRelation;
	}
	
	protected List<Relation> getOutputRelations() {
		return outputRelations;
	}
	
	protected void addRelationtoOutputList(final Relation relation) {
		outputRelations.add(relation);
	}
	
	protected boolean getOutputMsgFlag() {
		return outputMsgFlag;
	}
	
	protected void setOutputMsgFlag(final boolean flag) {
		outputMsgFlag = flag;
	}

	protected String getOutputMsg() {
		return outputMsg;
	}
	
	protected void setOutputMsg(final String outputMsg) {
		this.outputMsg = outputMsg;
	}
	
	protected void appendOutputMsg(final String appendedMsg) {
		this.outputMsg = this.outputMsg + appendedMsg;
	}
	
	protected abstract void decompose();
	

}
