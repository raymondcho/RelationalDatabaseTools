package relationalDatabaseTools.client;

import java.util.List;

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

	@Override
	public int compareTo(Closure otherClosure) {
		return this.closureOf.size() - otherClosure.closureOf.size();
	}
}
