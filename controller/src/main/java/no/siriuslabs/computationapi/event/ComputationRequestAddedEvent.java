package no.siriuslabs.computationapi.event;

import no.siriuslabs.computationapi.api.model.request.ComputationRequest;

/**
 * Event based on AbstractDataWorkflowEvent that can be fired when a new ComputationRequest has reached the system. The event carries the new ComputationRequest.
 */
public class ComputationRequestAddedEvent extends AbstractDataWorkflowEvent {

	/**
	 * ComputationRequest that was added to the system.
	 */
	private final ComputationRequest computationRequest;

	/**
	 * Constructor accepting the event's source and the ComputationRequest.
	 */
	public ComputationRequestAddedEvent(Object source, ComputationRequest computationRequest) {
		super(source);
		this.computationRequest = computationRequest;
	}

	public ComputationRequest getComputationRequest() {
		return computationRequest;
	}

	@Override
	public String toString() {
		return "ComputationRequestAddedEvent{" +
				"computationRequest=" + computationRequest +
				'}';
	}
}
