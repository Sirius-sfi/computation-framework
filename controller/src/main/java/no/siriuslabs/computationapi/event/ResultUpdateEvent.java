package no.siriuslabs.computationapi.event;

import no.siriuslabs.computationapi.api.model.computation.WorkPackageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultUpdateEvent extends AbstractDataWorkflowEvent {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResultUpdateEvent.class);

	private final WorkPackageResult workPackageResult;

	public ResultUpdateEvent(Object source, WorkPackageResult workPackageResult) {
		super(source);
		this.workPackageResult = workPackageResult;
	}

	public WorkPackageResult getWorkPackageResult() {
		return workPackageResult;
	}

	@Override
	public String toString() {
		return "ResultUpdateEvent{" +
				", workPackageResult=" + workPackageResult +
				'}';
	}
}
