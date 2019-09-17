package no.siriuslabs.computationapi.event;

import no.siriuslabs.computationapi.api.model.computation.DomainType;
import no.siriuslabs.computationapi.api.model.computation.WorkPackage;
import no.siriuslabs.computationapi.api.model.request.ComputationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DataPreparartionFinishedEvent extends AbstractDataWorkflowEvent {

	private static final Logger LOGGER = LoggerFactory.getLogger(DataPreparartionFinishedEvent.class);

	private final ComputationRequest request;
	private final List<WorkPackage> workPackages;

	public DataPreparartionFinishedEvent(Object source, ComputationRequest request, List<WorkPackage> workPackages) {
		super(source);
		this.request = request;
		this.workPackages = workPackages;
	}

	public ComputationRequest getRequest() {
		return request;
	}

	public DomainType getDomain() {
		return request.getDomain();
	}

	public List<WorkPackage> getWorkPackages() {
		return workPackages;
	}

	@Override
	public String toString() {
		return "DataPreparartionFinishedEvent{" +
				"domain=" + request.getDomain() +
				", workPackages=" + workPackages +
				'}';
	}
}
