package no.siriuslabs.computationapi.demo;

import no.siriuslabs.computationapi.api.model.computation.DomainType;

/**
 * DomainType implementation for the Demo application.
 */
// used reflective only
public enum DemoDomainType implements DomainType {

	DEMO;

	@Override
	public String getDomainType() {
		return name();
	}

}
