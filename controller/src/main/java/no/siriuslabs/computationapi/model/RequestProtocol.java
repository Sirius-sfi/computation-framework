package no.siriuslabs.computationapi.model;

import no.siriuslabs.computationapi.api.model.computation.DomainType;
import no.siriuslabs.computationapi.api.model.computation.WorkPackage;
import no.siriuslabs.computationapi.api.model.computation.WorkPackageResult;
import no.siriuslabs.computationapi.api.model.request.ComputationRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RequestProtocol {

	private final DomainType domain;
	private final ComputationRequest computationRequest;

	private List<WorkPackage> workPackages;
	private List<WorkPackageResult> workPackageResults;

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
