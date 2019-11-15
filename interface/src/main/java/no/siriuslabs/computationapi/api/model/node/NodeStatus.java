package no.siriuslabs.computationapi.api.model.node;

/**
 * Enum representing the possible states a WorkerNode can have.
 */
public enum NodeStatus {

	READY,
	RESERVED,
	BUSY,
	DONE,
	SUSPICIOUS,
	UNAVAILABLE;

}
