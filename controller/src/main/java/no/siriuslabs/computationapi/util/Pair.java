package no.siriuslabs.computationapi.util;

import java.util.Objects;

/**
 * Class representing a key-value-pair.<p>
 * Both first-/key-part and second-/value-part can only be set once during object construction. They can be queried at any time using the getter-methods.<p>
 * Both equals() and hashCode() methods are fully implemented, so that usage of the Pair in the keys of Collections classes should be possible without problems
 * or side-effects if instances of the parameterized classes also show the necessary behaviour in this regard.
 *
 * @param <X> Any class to represent the key-part of the pair.
 * @param <Y> Any class to represent the value-part of the pair.
 */
public class Pair<X,Y> {

	/**
	 * The first or key-part of the pair.
	 */
	private final X x;
	/**
	 * The second or value-part of the pair.
	 */
	private final Y y;

	/**
	 * Constructor expecting both parts.
	 */
	public Pair(X x, Y y) {
		this.x = x;
		this.y = y;
	}

	public X getX() {
		return x;
	}

	public Y getY() {
		return y;
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) {
			return true;
		}
		if(o == null || getClass() != o.getClass()) {
			return false;
		}
		Pair<?, ?> pair = (Pair<?, ?>) o;
		return Objects.equals(x, pair.x) &&
				Objects.equals(y, pair.y);
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	@Override
	public String toString() {
		return "Pair{" +
				"x=" + x +
				", y=" + y +
				'}';
	}
}
