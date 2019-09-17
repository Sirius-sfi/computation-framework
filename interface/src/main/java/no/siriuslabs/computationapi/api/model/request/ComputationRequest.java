package no.siriuslabs.computationapi.api.model.request;

import no.siriuslabs.computationapi.api.model.computation.DomainType;

public class ComputationRequest {

	private DomainType domain;
	private Payload payload;
	private long startedTimestamp;
	private long preparationTime;

	/**
	 * Constructor needed for de-serialization.
	 */
	public ComputationRequest() {
	}

	public DomainType getDomain() {
		return domain;
	}

	public void setDomain(DomainType domain) {
		this.domain = domain;
	}

	public Payload getPayload() {
		return payload;
	}

	public void setPayload(Payload payload) {
		this.payload = payload;
	}

	public long getStartedTimestamp() {
		return startedTimestamp;
	}

	public void setStartedTimestamp(long startedTimestamp) {
		this.startedTimestamp = startedTimestamp;
	}

	public long getPreparationTime() {
		return preparationTime;
	}

	public void setPreparationTime(long preparationTime) {
		this.preparationTime = preparationTime;
	}

	@Override
	public String toString() {
		return "ComputationRequest{" +
				"domain=" + domain +
				", payload=" + payload +
				'}';
	}
}
