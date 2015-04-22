package relationalDatabaseTools.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FunctionalDependency implements Comparable<FunctionalDependency> {
	private final Relation relation;
	private final List<Attribute> leftSide;
	private final List<Attribute> rightSide;
	private String name;
	public boolean isProperFD;
	
	public FunctionalDependency(final String input, final Relation relation) {
		this.relation = relation;
		leftSide = new ArrayList<>();
		rightSide = new ArrayList<>();
		isProperFD = initialize(input);
		if (leftSide.isEmpty() || rightSide.isEmpty()) {
			isProperFD = false;
		}
	}
	
	public FunctionalDependency(final List<Attribute> leftHandSide, final List<Attribute> rightHandSide, final Relation relation) {
		this.relation = relation;
		this.leftSide = leftHandSide;
		this.rightSide = rightHandSide;
		isProperFD = true;
		if (leftSide.isEmpty() || rightSide.isEmpty()) {
			isProperFD = false;
		}
		if (isProperFD) {
			try {
				Collections.sort(leftSide);
				Collections.sort(rightSide);
				setName();
			} catch (Exception e) {
				isProperFD = false;
			}
		}
	}
	
	private boolean initialize(final String input) {
		try {
			setAttributes(input);
			Collections.sort(leftSide);
			Collections.sort(rightSide);
			setName();
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
	
	private void setName() {
		StringBuilder sb = new StringBuilder();
		for (Attribute a : leftSide) {
			sb.append(a.getName());
			sb.append(",");
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append("->");
		for (Attribute b : rightSide) {
			sb.append(b.getName());
			sb.append(",");
		}
		sb.deleteCharAt(sb.length()-1);
		name = sb.toString();
	}
	public String getFDName() {
		return name;
	}
	public List<Attribute> getLeftHandAttributes() {
		return leftSide;
	}
	public List<Attribute> getRightHandAttributes() {
		return rightSide;
	}

	@Override
	public int compareTo(FunctionalDependency otherFD) {
		if (this.leftSide.size() != otherFD.leftSide.size()) {
			return this.leftSide.size() - otherFD.leftSide.size();
		}
		return this.getFDName().compareTo(otherFD.getFDName());
	}
}
