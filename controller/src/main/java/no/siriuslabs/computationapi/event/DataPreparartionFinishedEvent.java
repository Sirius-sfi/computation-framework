package no.siriuslabs.computationapi.event;

import no.siriuslabs.computationapi.api.model.computation.DomainType;
import no.siriuslabs.computationapi.api.model.computation.WorkPackage;
import no.siriuslabs.computationapi.api.model.request.ComputationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Event based on AbstractDataWorkflowEvent that can be fired when the data preparation phase is finished and WorkPackages have been generated.
 * The event carries the ComputationRequest that started the computation run and the generated WorkPackages.
 */
public class DataPreparartionFinishedEvent extends AbstractDataWorkflowEvent {

	/**
	 * ComputationRequest that triggered WorkPackage generation.
	 */
	private final ComputationRequest request;
	/**
	 * All WorkPackages generated during the preparation phase.
	 */
	private final List<WorkPackage> workPackages;

	/**
	 * Constructor accepting the event's source, the ComputationRequest and the WorkPackages.
	 */
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
