package no.siriuslabs.computationapi.api.model.computation;

import java.util.Map;

/**
 * Container class representing the result of the computation run after all work packages are finished.
 * It contains the final status of the computation and may contain an error message (if something went wrong) and a collection of resulting data (if any).
 */
public class ComputationResult {

	/**
	 * Final status of this computation run.
 	 */
	private Status status;

	/**
	 * Optional error message - usually only if an error prevented finishing and producing results.
	 */
	private String errorMessage;

	/**
	 * Optional results of this computation as key-value-pairs.
	 */
	private Map<String, Object> results;

	/**
	 * Constructor needed for de-serialization.
	 */
	public ComputationResult() {
	}

	/**
	 * Constructor accepting status and error message.
	 */
	public ComputationResult(Status status, String errorMessage) {
		this.status = status;
		this.errorMessage = errorMessage;
	}

	/**
	 * Constructor accepting status and map of result data.
	 */
	public ComputationResult(Status status, Map<String, Object> results) {
		this.status = status;
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
