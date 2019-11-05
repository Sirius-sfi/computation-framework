package no.siriuslabs.computationapi.api.model.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import no.siriuslabs.computationapi.api.model.computation.DomainType;
import no.siriuslabs.computationapi.api.model.computation.DomainTypeToStringConverter;
import no.siriuslabs.computationapi.api.model.computation.StringToDomainTypeConverter;

public class ComputationRequest {

	@JsonSerialize(converter = DomainTypeToStringConverter.class)
	@JsonDeserialize(converter = StringToDomainTypeConverter.class)
	private DomainType domain;

	private Payload payload;
	private long startedTimestamp;
	private long preparationTime;
	private int numberNodesStart;
	private int numberWPs;

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

	public int getNumberNodesStart() {
		return numberNodesStart;
	}

	public void setNumberNodesStart(int numberNodesStart) {
		this.numberNodesStart = numberNodesStart;
	}

	public int getNumberWPs() {
		return numberWPs;
	}

	public void setNumberWPs(int numberWPs) {
		this.numberWPs = numberWPs;
	}

	@Override
	public String toString() {
		return "ComputationRequest{" +
				"domain=" + domain +
				", payload=" + payload +
				'}';
	}
}
