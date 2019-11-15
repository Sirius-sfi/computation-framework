package no.siriuslabs.computationapi.api.model.computation;

import java.util.Map;

/**
 * Container class representing the computation result of a single WorkPackage.<p>
 * The result contains the original WorkPackage that lead to its creation, a collection of domain specific result data and some statistical information about the computation.
 */
public class WorkPackageResult {

	/**
	 * The original WorkPackage this result is based on.
	 */
	private WorkPackage workPackage;

	/**
	 * Map of domain specific result data.
	 */
	private Map<String, Object> data;

	/**
	 * Running time of the work package computation.
	 */
	private long runningTime;
	/**
	 * Timestamp the computation finished.
	 */
	private long finishedTimestamp;

	/**
	 * ID of the WorkerNode the computation was run on.
	 */
	private String nodeId;

	/**
	 * Constructor needed for de-serialization.
	 */
	public WorkPackageResult() {
	}

	/**
	 * Constructor expecting the WorkPackage this result is based on.
	 */
	public WorkPackageResult(WorkPackage workPackage) {
		this.workPackage = workPackage;
	}

	public WorkPackage getWorkPackage() {
		return workPackage;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	public long getRunningTime() {
		return runningTime;
	}

	public void setRunningTime(long runningTime) {
		this.runningTime = runningTime;
	}

	public long getFinishedTimestamp() {
		return finishedTimestamp;
	}

	public void setFinishedTimestamp(long finishedTimestamp) {
		this.finishedTimestamp = finishedTimestamp;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	@Override
	public String toString() {
		return "WorkPackageResult{" +
				"workPackage=" + workPackage +
				", data=" + data +
				'}';
	}
}
