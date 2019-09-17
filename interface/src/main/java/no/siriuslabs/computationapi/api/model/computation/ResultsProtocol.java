package no.siriuslabs.computationapi.api.model.computation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ResultsProtocol {

	private DomainType domain;

	private long startedTimestamp;
	private long finishedTimestamp;

	private long preparationTime;

	private long minWpTime;
	private long maxWpTime;
	private long avgWpTime;

	private List<WorkPackageResult> workPackageResults;

	/**
	 * Constructor needed for de-serialization.
	 */
	public ResultsProtocol() {
	}

	public ResultsProtocol(DomainType domain, List<WorkPackageResult> workPackageResults) {
		this.domain = domain;
		this.workPackageResults = new ArrayList<>(workPackageResults);
	}

	public DomainType getDomain() {
		return domain;
	}

	public long getStartedTimestamp() {
		return startedTimestamp;
	}

	public void setStartedTimestamp(long startedTimestamp) {
		this.startedTimestamp = startedTimestamp;
	}

	public long getFinishedTimestamp() {
		return finishedTimestamp;
	}

	public void setFinishedTimestamp(long finishedTimestamp) {
		this.finishedTimestamp = finishedTimestamp;
	}

	public long getPreparationTime() {
		return preparationTime;
	}

	public void setPreparationTime(long preparationTime) {
		this.preparationTime = preparationTime;
	}

	public long getMinWpTime() {
		return minWpTime;
	}

	public void setMinWpTime(long minWpTime) {
		this.minWpTime = minWpTime;
	}

	public long getMaxWpTime() {
		return maxWpTime;
	}

	public void setMaxWpTime(long maxWpTime) {
		this.maxWpTime = maxWpTime;
	}

	public long getAvgWpTime() {
		return avgWpTime;
	}

	public void setAvgWpTime(long avgWpTime) {
		this.avgWpTime = avgWpTime;
	}

	public List<WorkPackageResult> getWorkPackageResults() {
		return Collections.unmodifiableList(workPackageResults);
	}

	public void addWorkPackageResults(WorkPackageResult... workPackageResults) {
		this.workPackageResults.addAll(Arrays.asList(workPackageResults));
	}
}
