package no.siriuslabs.computationapi.api.model.computation;

import java.util.Map;

public class WorkPackageResult {

	private WorkPackage workPackage;

	private Map<String, Object> data;

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

	@Override
	public String toString() {
		return "WorkPackageResult{" +
				"workPackage=" + workPackage +
				", data=" + data +
				'}';
	}
}
