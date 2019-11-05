package no.siriuslabs.computationapi.api.model.computation;

import java.util.Objects;

public class DomainTypeImpl implements DomainType {

	private final String domainType;

	public DomainTypeImpl(String domainType) {
		this.domainType = domainType;
	}

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
