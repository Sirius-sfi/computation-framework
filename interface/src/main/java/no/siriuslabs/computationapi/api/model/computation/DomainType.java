package no.siriuslabs.computationapi.api.model.computation;

/**
 * Interface representing a type of domain (use case, type of computation) on both the controller side of the framework as well as on the implementation (worker node) side.<p>
 * Concrete implementations on the implementation side may be enums (which each could even contain multiple related domain types) or regular classes implementing this interface.<p>
 * <b>Caution:</b> Any non-enum implementation must override equals() and hashCode() using the domain type's string representation as a base for identity, otherwise results can be "unexpected"!
 */
public interface DomainType {

	/**
	 * Returns a string representation of this DomainType.
	 */
	String getDomainType();

}
