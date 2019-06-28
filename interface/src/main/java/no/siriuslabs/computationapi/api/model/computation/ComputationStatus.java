package no.siriuslabs.computationapi.api.model.computation;

public class ComputationStatus {

	private Status status;
	private int percentDone;
	private int packagesToDo;

	public ComputationStatus(Status status, int percentDone, int packagesToDo) {
		this.status = status;
		this.percentDone = percentDone;
		this.packagesToDo = packagesToDo;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public int getPercentDone() {
		return percentDone;
	}

	public void setPercentDone(int percentDone) {
		this.percentDone = percentDone;
	}

	public int getPackagesToDo() {
		return packagesToDo;
	}

	public void setPackagesToDo(int percentDone) {
		this.packagesToDo = percentDone;
	}

	@Override
	public String toString() {
		return "ComputationStatus{" +
				"status=" + status +
				", percentDone=" + percentDone +
				", packagesToDo=" + packagesToDo +
				'}';
	}
}
