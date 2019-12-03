package no.siriuslabs.computationapi.event;

import org.springframework.context.ApplicationEvent;

/**
 * Common superclass of all controller workflow events.
 */
public abstract class AbstractDataWorkflowEvent extends ApplicationEvent {

	/**
	 * Constructor accepting the event's source.
	 */
	protected AbstractDataWorkflowEvent(Object source) {
		super(source);
	}

}
