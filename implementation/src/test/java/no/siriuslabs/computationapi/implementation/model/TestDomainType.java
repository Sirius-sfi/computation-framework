package no.siriuslabs.computationapi.implementation.model;

import no.siriuslabs.computationapi.api.model.computation.DomainType;

/**
 * DomainType implementing enum for use in unit tests.
 */
public enum TestDomainType implements DomainType {

	TEST_1;

	@Override
	public String getDomainType() {
		return name();
	}
}
