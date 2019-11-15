package no.siriuslabs.computationapi.api.model.computation;

import no.siriuslabs.computationapi.api.model.request.ComputationRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Container class representing the computation history of a computation request from the original request over each WorkPackage generated from that request up until all the results of every WorkPackage are collected.
 */
public class RequestProtocol {

	/**
	 * Domain type the computation run belongs to.
	 */
	private DomainType domain;

	/**
	 * The initial ComputationRequest that started the computation run.
	 */
	private ComputationRequest computationRequest;

	/**
	 * All WorkPackages generated from the request data.
	 */
	private List<WorkPackage> workPackages;

	/**
	 * Results for every WorkPackage that was run on a worker node yet.
	 */
	private List<WorkPackageResult> workPackageResults;

	/**
	 * Constructor needed for de-serialization.
	 */
	public RequestProtocol() {
	}

	/**
	 * Constructor accepting DomainType and the initial ComputationRequest.
	 */
	public RequestProtocol(DomainType domain, ComputationRequest computationRequest) {
		this.domain = domain;
		this.computationRequest = computationRequest;
		workPackages = new ArrayList<>();
		workPackageResults = new ArrayList<>();
	}

	public DomainType getDomain() {
		return domain;
	}

	public ComputationRequest getComputationRequest() {
		return computationRequest;
	}

	public List<WorkPackage> getWorkPackages() {
		return Collections.unmodifiableList(workPackages);
	}

	/**
	 * Adds the given WorkPackages to the total amount of WorkPackages available.
	 */
	public void addWorkPackages(List<WorkPackage> workPackages) {
		this.workPackages.addAll(workPackages);
	}

	/**
	 * Adds the given WorkPackages to the total amount of WorkPackages available.
	 */
	public void addWorkPackages(WorkPackage... workPackages) {
		this.workPackages.addAll(Arrays.asList(workPackages));
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
