package no.siriuslabs.computationapi.api.model.computation;

import java.util.Map;

public class ComputationResult {

	private Status status;
	private String errorMessage;

	private RequestProtocol protocol;
	private Map<String, Object> results;

	/**
	 * Constructor needed for de-serialization.
	 */
	public ComputationResult() {
	}

	public ComputationResult(Status status, String errorMessage) {
		this.status = status;
		this.errorMessage = errorMessage;
	}

	public ComputationResult(Status status, RequestProtocol protocol, Map<String, Object> results) {
		this.status = status;
		this.protocol = protocol;
		this.results = results;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public RequestProtocol getProtocol() {
		return protocol;
	}

	public void setProtocol(RequestProtocol protocol) {
		this.protocol = protocol;
	}

	public Map<String, Object> getResults() {
		return results;
	}

	public void setResults(Map<String, Object> results) {
		this.results = results;
	}

	@Override
	public String toString() {
		return "ComputationResult{" +
				"status=" + status +
				", errorMessage='" + errorMessage + '\'' +
				", results=" + results +
				'}';
	}
}
