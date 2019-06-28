package no.siriuslabs.computationapi.api.model.computation;

public class ComputationResult {

	private Status status;
	private String errorMessage;
	// TODO payload


	public ComputationResult(Status status, String errorMessage) {
		this.status = status;
		this.errorMessage = errorMessage;
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

	@Override
	public String toString() {
		return "ComputationResult{" +
				"status=" + status +
				", errorMessage='" + errorMessage + '\'' +
				'}';
	}
}
