package no.siriuslabs.computationapi.model;

import no.siriuslabs.computationapi.api.model.computation.DomainType;

public enum TestDomainType implements DomainType {

	TEST_1,
	TEST_2;

	@Override
	public String getDomainType() {
		return name();
	}
}
