package no.siriuslabs.computationapi.api.model.computation;

import java.util.Objects;

/**
 * Concrete implementation of the DomainType interface for use by the controller side.<p>
 * The controller uses this string-based general implementation, as it is not supposed to know the worker node's implementations directly.<p>
 * This class is in the interface module because of its use by the Spring converter classes, which again are referenced in various container classes in this module.
 */
public class DomainTypeImpl implements DomainType {

	/**
	 * String representation of the DomainType.
	 */
	private final String domainType;

	/**
	 * Constructor expecting the string representation of the domain type.
	 */
	public DomainTypeImpl(String domainType) {
		this.domainType = domainType;
	}

	@Override
	public String getDomainType() {
		return domainType;
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) {
			return true;
		}
		if(o == null || getClass() != o.getClass()) {
			return false;
		}
		DomainTypeImpl that = (DomainTypeImpl) o;
		return domainType.equals(that.domainType);
	}

	@Override
	public int hashCode() {
		return Objects.hash(domainType);
	}

	@Override
	public String toString() {
		return domainType == null ? "" : domainType.toUpperCase();
	}

}
