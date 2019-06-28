package no.siriuslabs.computationapi.event;

import no.siriuslabs.computationapi.api.model.request.ComputationRequest;

public class ComputationRequestAddedEvent extends AbstractDataWorkflowEvent {

	private final ComputationRequest computationRequest;

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
