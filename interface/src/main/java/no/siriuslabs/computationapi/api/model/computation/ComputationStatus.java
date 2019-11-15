package no.siriuslabs.computationapi.api.model.computation;

/**
 * Container class representing current the status of a computation run.
 * It contains information about status, overall percentage done and number of packages still to do.
 */
public class ComputationStatus {

	/**
	 * Current status of the computation run.
	 */
	private Status status;

	/**
	 * Percentage done of all work packages of this run.
	 */
	private int percentDone;

	/**
	 * Number of work packages still to be computed (not finished).
	 */
	private int packagesToDo;

	/**
	 * Constructor expecting all values (status, percentage done and number to do).
	 */
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
