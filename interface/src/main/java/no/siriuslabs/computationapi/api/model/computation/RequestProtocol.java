package no.siriuslabs.computationapi.api.model.computation;

import no.siriuslabs.computationapi.api.model.request.ComputationRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RequestProtocol {

	private DomainType domain;
	private ComputationRequest computationRequest;

	private List<WorkPackage> workPackages;
	private List<WorkPackageResult> workPackageResults;

	/**
	 * Constructor needed for de-serialization.
	 */
	public RequestProtocol() {
	}

	public RequestProtocol(DomainType domain, ComputationRequest computationRequest) {
		this.domain = domain;
		this.computationRequest = computationRequest;
		workPackages = new ArrayList<>();
		workPackageResults = new ArrayList<>();
	}

	// TODO should only be temporary
	public void cleanup() {
		computationRequest = null;
		workPackages.clear();
		workPackageResults.clear();
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

	public void addWorkPackages(List<WorkPackage> workPackages) {
		this.workPackages.addAll(workPackages);
	}

	public void addWorkPackages(WorkPackage... workPackages) {
		this.workPackages.addAll(Arrays.asList(workPackages));
	}

	public List<WorkPackageResult> getWorkPackageResults() {
		return Collections.unmodifiableList(workPackageResults);
	}

	public void addWorkPackageResults(WorkPackageResult... workPackageResults) {
		this.workPackageResults.addAll(Arrays.asList(workPackageResults));
	}
}
