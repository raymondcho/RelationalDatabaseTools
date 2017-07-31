package relationalDatabaseTools.client;

/**
 * Attribute class that represents an attribute.
 * @author Raymond Cho
 *
 */
public class Attribute implements Comparable<Attribute>{
	private final String name;
	public Attribute(final String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	@Override
	public String toString() {
		return name;
	}
	@Override
	public int compareTo(Attribute otherAttribute) {
		if (this.name.length() != otherAttribute.getName().length()) {
			return this.name.length() - otherAttribute.getName().length();
		}
		return this.getName().compareTo(otherAttribute.getName());
	}
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Attribute)) {
			return false;
		}
		Attribute otherAttribute = (Attribute) o;
		return this.name.compareTo(otherAttribute.name) == 0;
	}
	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
