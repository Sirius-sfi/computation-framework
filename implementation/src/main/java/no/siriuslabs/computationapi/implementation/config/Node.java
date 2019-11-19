package no.siriuslabs.computationapi.implementation.config;

import no.siriuslabs.computationapi.api.model.computation.DomainType;

/**
 * Configuration container class representing node related information. Currently only information about the DomainType of the node.
 */
public class Node {

	/**
	 * DomainType supported by this node.
	 * @see ConfigDomainTypeConverter
	 */
	private DomainType domain;

	public DomainType getDomain() {
		return domain;
	}

	public void setDomain(DomainType domain) {
		this.domain = domain;
	}
}
