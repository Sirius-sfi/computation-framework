package no.siriuslabs.computationapi.api.model.computation;

import java.util.Map;

public class WorkPackageResult {

	private WorkPackage workPackage;

	private Map<String, Object> data;

	private long runningTime;
	private long finishedTimestamp;

	private String nodeId;

	/**
	 * Constructor needed for de-serialization.
	 */
	public WorkPackageResult() {
	}

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
