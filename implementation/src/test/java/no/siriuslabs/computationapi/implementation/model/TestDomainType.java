package no.siriuslabs.computationapi.implementation.model;

import no.siriuslabs.computationapi.api.model.computation.DomainType;

public enum TestDomainType implements DomainType {

	TEST_1,
	TEST_2;

	@Override
	public String getDomainType() {
		return name();
	}
}
