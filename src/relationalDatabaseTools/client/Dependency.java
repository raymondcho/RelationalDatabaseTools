package relationalDatabaseTools.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Dependency<T extends Dependency<T>> implements Comparable<T> {
	private final Relation relation;
	private final List<Attribute> leftSide;
	private final List<Attribute> rightSide;
	private String name;
	private final String dependencyArrow;
	private boolean isProperDependency;
	
	public Dependency(final String input, final String dependencyArrow, final Relation relation) {
		this.relation = relation;
		leftSide = new ArrayList<>();
		rightSide = new ArrayList<>();
		name = null;
		this.dependencyArrow = dependencyArrow;
		isProperDependency = initialize(input);
	}
	
	public Dependency(final List<Attribute> leftHandSide, final List<Attribute> rightHandSide, final String dependencyArrow, final Relation relation) {
		this.relation = relation;
		this.leftSide = leftHandSide;
		this.rightSide = rightHandSide;
		name = null;
		this.dependencyArrow = dependencyArrow;
		isProperDependency = true;
		if (leftSide.isEmpty() || rightSide.isEmpty()) {
			isProperDependency = false;
		}
		if (isProperDependency) {
			try {
				Collections.sort(leftSide);
				Collections.sort(rightSide);
				setName(this.dependencyArrow);
			} catch (Exception e) {
				isProperDependency = false;
			}
		}
	}
	
	private boolean initialize(final String input) {
		try {
			setAttributes(input);
			if (leftSide.isEmpty() || rightSide.isEmpty()) {
				return false;
			}
			Collections.sort(leftSide);
			Collections.sort(rightSide);
			setName(dependencyArrow);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	private void setAttributes(final String input) {
		String[] splitted = input.split("->");
		String fullLeft = splitted[0];
		String fullRight = splitted[1];
		
		// Get left attributes
		String[] leftAttributes = fullLeft.split(",");
		for (String attribute : leftAttributes) {
			if (!attribute.isEmpty()) {
				Attribute a = relation.getAttribute(attribute);
				if (a == null) {
					relation.setIntegrityCheckErrorMsg("Attribute " + attribute + " does not exist in schema of Relation " + relation.getName());
					relation.setPassedIntegrityChecks(false);
					return;
				}
				leftSide.add(a);
			}
		}
		
		// Get right attributes
		String[] rightAttributes = fullRight.split(",");
		for (String attribute : rightAttributes) {
			if (!attribute.isEmpty()) {
				Attribute a = relation.getAttribute(attribute);
				if (a == null) {
					relation.setIntegrityCheckErrorMsg("Attribute " + attribute + " does not exist in schema of Relation " + relation.getName());
					relation.setPassedIntegrityChecks(false);
					return;
				}
				rightSide.add(a);
			}
		}
	}
	
	/**
	 * Given left hand attribute A and right hand attribute B and dependencyArrow, 
	 * sets dependency name to be "A {dependencyArrow} B".
	 * Will only set the name once in object's lifetime.
	 * @param dependencyArrow
	 */
	private void setName(final String dependencyArrow) {
		if (this.name != null) {
			return;		// Makes name immutable once set
		}
		StringBuilder sb = new StringBuilder();
		for (Attribute a : leftSide) {
			sb.append(a.getName());
			sb.append(",");
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append(" " + dependencyArrow + " ");
		for (Attribute b : rightSide) {
			sb.append(b.getName());
			sb.append(",");
		}
		sb.deleteCharAt(sb.length()-1);
		this.name = sb.toString();
	}
	
	protected String getName() {
		return this.name;
	}
	
	protected String getLeftHandNameKey() {
		StringBuilder sb = new StringBuilder();
		for (Attribute a : leftSide) {
			sb.append(a.getName());
			sb.append(":");
		}
		sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
	
	protected List<Attribute> getLeftHandAttributes() {
		return leftSide;
	}
	
	protected List<Attribute> getRightHandAttributes() {
		return rightSide;
	}
	
	protected boolean getIsProperDependency() {
		return isProperDependency;
	}
	
	public abstract int compareTo(T otherDependency);
}
