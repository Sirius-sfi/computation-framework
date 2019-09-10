package no.siriuslabs.computationapi.api.model.computation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ResultsProtocol {

	private DomainType domain;

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

	public List<WorkPackageResult> getWorkPackageResults() {
		return Collections.unmodifiableList(workPackageResults);
	}

	public void addWorkPackageResults(WorkPackageResult... workPackageResults) {
		this.workPackageResults.addAll(Arrays.asList(workPackageResults));
	}
}
