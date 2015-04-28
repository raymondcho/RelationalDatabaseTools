package relationalDatabaseTools.client;

import java.util.List;

/**
 * Represents a closure.
 * @author Raymond Cho
 *
 */
public class Closure implements Comparable<Closure>{
	private final List<Attribute> closureOf;		// Left side of closure
	private final List<Attribute> closure;			// Right side of closure
	
	public Closure(final List<Attribute> leftSide, final List<Attribute> rightSide) {
		this.closureOf = leftSide;
		this.closure = rightSide;
	}
	
	public List<Attribute> getClosureOf() {
		return closureOf;
	}
	
	public List<Attribute> getClosure() {
		return closure;
	}
	
	public String printLeftSideAttributes() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < closureOf.size(); i++) {
			sb.append(closureOf.get(i).getName());
			if (i < closureOf.size() - 1) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}
	
	public String printCompleteClosure() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append(printLeftSideAttributes());
		sb.append("}+ -> {");
		for (int i = 0; i < closure.size(); i++) {
			sb.append(closure.get(i).getName());
			if (i < closure.size() - 1) {
				sb.append(", ");
			}
		}
		sb.append("}");
		return sb.toString();
	}

	@Override
	public int compareTo(Closure otherClosure) {
		return this.closureOf.size() - otherClosure.closureOf.size();
	}
}
