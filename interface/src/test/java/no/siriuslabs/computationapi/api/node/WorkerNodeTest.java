package no.siriuslabs.computationapi.api.node;

import no.siriuslabs.computationapi.api.model.node.NodeStatus;
import no.siriuslabs.computationapi.api.model.node.WorkerNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Set of tests for behaviour of the WorkerNode class.
 */
public class WorkerNodeTest {

	@DisplayName("Test node property values after initialization")
	@Test
	public void testAfterInitialization() {
		WorkerNode node = new WorkerNode();

		assertNull(node.getLastStatusChange(), "Status change date must be null after initialization");
		assertEquals(NodeStatus.UNAVAILABLE, node.getStatus(), "Initial node status after initialization must be UNAVAILABLE");
	}

	@DisplayName("Test node property values after initialization")
	@Test
	public void testAfterChangeOfStatus() {
		WorkerNode node = new WorkerNode();
		node.setStatus(NodeStatus.READY);

		assertNotNull(node.getLastStatusChange(), "Last status change date must not be null after changing node status");
	}

}
