package no.siriuslabs.computationapi.event;

import org.springframework.context.ApplicationEvent;

public abstract class AbstractDataWorkflowEvent extends ApplicationEvent {

	protected AbstractDataWorkflowEvent(Object source) {
		super(source);
	}

}
