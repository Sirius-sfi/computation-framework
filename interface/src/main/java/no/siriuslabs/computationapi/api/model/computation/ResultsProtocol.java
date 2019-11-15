package no.siriuslabs.computationapi.api.model.computation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Container class representing all results from a computation run plus some statistical data used to report the results back to the caller.<p>
 * This class is quite similar to RequestProtocol but has the amount of data cut down to only the results for performance reasons.
 */
public class ResultsProtocol {

	/**
	 * Domain type the computation run belongs to.
	 */
	@JsonSerialize(converter = DomainTypeToStringConverter.class)
	@JsonDeserialize(converter = StringToDomainTypeConverter.class)
	private DomainType domain;

	/**
	 * Timestamp the computation run started.
	 */
	private long startedTimestamp;
	/**
	 * Timestamp the computation run ended.
	 */
	private long finishedTimestamp;

	/**
	 * Amount of time taken to perform the data preparation step (prepareAndPackageData).
	 */
	private long preparationTime;

	/**
	 * Shortest amount of time taken to compute a WorkPackage.
	 */
	private long minWpTime;
	/**
	 * Longest amount of time taken to compute a WorkPackage.
	 */
	private long maxWpTime;
	/**
	 * Average amount of time taken to compute a WorkPackage.
	 */
	private long avgWpTime;

	/**
	 * Number of WorkerNodes at the beginning of the computation run (after the preparation step).
	 */
	private int numberNodesStart;
	/**
	 * Number of WorkerNodes at the end of the computation run (when accumulating the results).
	 */
	private int numberNodesEnd;

	/**
	 * Overall number of WorkPackages.
	 */
	private int numberWPs;

	/**
	 * Results for every WorkPackage that was run on a worker node yet.
	 */
	private List<WorkPackageResult> workPackageResults;

	/**
	 * Constructor needed for de-serialization.
	 */
	public ResultsProtocol() {
	}

	/**
	 * Constructor accepting DomainType and WorkPackageResults.
	 */
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

	public int getNumberNodesStart() {
		return numberNodesStart;
	}

	public void setNumberNodesStart(int numberNodesStart) {
		this.numberNodesStart = numberNodesStart;
	}

	public int getNumberNodesEnd() {
		return numberNodesEnd;
	}

	public void setNumberNodesEnd(int numberNodesEnd) {
		this.numberNodesEnd = numberNodesEnd;
	}

	public int getNumberWPs() {
		return numberWPs;
	}

	public void setNumberWPs(int numberWPs) {
		this.numberWPs = numberWPs;
	}

	public List<WorkPackageResult> getWorkPackageResults() {
		return Collections.unmodifiableList(workPackageResults);
	}

	/**
	 * Adds the given results to the total amount of WorkPackageResults available.
	 */
	public void addWorkPackageResults(WorkPackageResult... workPackageResults) {
		this.workPackageResults.addAll(Arrays.asList(workPackageResults));
	}
}
