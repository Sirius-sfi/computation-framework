package no.siriuslabs.computationapi.event;

import no.siriuslabs.computationapi.api.model.computation.WorkPackageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event based on AbstractDataWorkflowEvent that can be fired when the computation of a WorkPackage has finished.
 * The event carries the WorkPackageResult for that WorkPackage.
 */
public class ResultUpdateEvent extends AbstractDataWorkflowEvent {

	/**
	 * WorkPackageResult that was created when the computation finished.
	 */
	private final WorkPackageResult workPackageResult;

	/**
	 * Constructor accepting the event's source and the WorkPackageResult.
	 */
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
