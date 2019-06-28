package no.siriuslabs.computationapi.implementation.config;

import no.siriuslabs.computationapi.api.model.computation.DomainType;

public class Node {

	private DomainType domain;

	public DomainType getDomain() {
		return domain;
	}

	public void setDomain(DomainType domain) {
		this.domain = domain;
	}
}
