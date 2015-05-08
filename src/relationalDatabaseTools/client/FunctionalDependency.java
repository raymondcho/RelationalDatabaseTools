package relationalDatabaseTools.client;

import java.util.List;

/**
 * Represents a functional dependency.
 * @author Raymond Cho
 *
 */
public class FunctionalDependency extends Dependency<FunctionalDependency> {
	
	public FunctionalDependency(final String input, final Relation relation) {
		super(input, RDTUtils.functionalDependencyArrow, relation);
	}
	
	public FunctionalDependency(final List<Attribute> leftHandSide, final List<Attribute> rightHandSide, final Relation relation) {
		super(leftHandSide, rightHandSide, RDTUtils.functionalDependencyArrow, relation);
	}
	
	public String getFDName() {
		return getName();
	}

	@Override
	public int compareTo(FunctionalDependency otherDependency) {
		if (this.getLeftHandAttributes().size() != otherDependency.getLeftHandAttributes().size()) {
			return this.getLeftHandAttributes().size() - otherDependency.getLeftHandAttributes().size();
		}
		return this.getFDName().compareTo(otherDependency.getFDName());
	}

}
