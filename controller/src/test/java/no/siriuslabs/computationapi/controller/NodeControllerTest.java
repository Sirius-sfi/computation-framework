package no.siriuslabs.computationapi.controller;

import no.siriuslabs.computationapi.api.exception.InvalidParameterException;
import no.siriuslabs.computationapi.api.model.node.WorkerNode;
import no.siriuslabs.computationapi.service.NodeRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
public class NodeControllerTest {

	private static final String EXISTING_NODE_ID = "alreadyExists";
	private static final String NEW_NODE_ID = "thisOneIsNew";

	@Mock
	private NodeRegistry nodeRegistry;

	@InjectMocks
	private NodeController nodeController;

	@DisplayName("Test the controller side code of getNumberOfNodes() only")
	@Test
	public void testGetNumberOfNodes() {
		Mockito.when(nodeRegistry.getNumberOfNodes()).thenReturn(5);

		// we just want to see if it runs through
		assertAll(() -> { nodeController.getNumberOfNodes(); });
	}

	@DisplayName("Test the controller side code to register nodes")
	@Test
	public void testRegisterNode() {
		WorkerNode existingNode = new WorkerNode();
		existingNode.setId(EXISTING_NODE_ID);

		WorkerNode newNode = new WorkerNode();
		existingNode.setId(NEW_NODE_ID);

		Mockito.when(nodeRegistry.hasNode(existingNode)).thenReturn(true);
		Mockito.when(nodeRegistry.hasNode(newNode)).thenReturn(false);

		// test with already registered node
		ResponseEntity<Object> response = nodeController.registerNode(existingNode);
		assertEquals(HttpStatus.NOT_ACCEPTABLE, response.getStatusCode(), "Response status code not 406 as expected");

		// test with unregistered node
		response = nodeController.registerNode(newNode);
		assertEquals(HttpStatus.OK, response.getStatusCode(), "Response status code not 200 as expected");
	}

	@DisplayName("Test the controller side code to unregister nodes")
	@Test
	public void testUnregisterNode() {
		Mockito.when(nodeRegistry.hasNode(EXISTING_NODE_ID)).thenReturn(true);
		Mockito.when(nodeRegistry.hasNode(NEW_NODE_ID)).thenReturn(false);

		assertThrows(InvalidParameterException.class, () -> { nodeController.unregisterNode(NEW_NODE_ID); }, "Removing unknown node is expected to trigger exception");
		assertAll(() -> { nodeController.unregisterNode(EXISTING_NODE_ID); });
	}

}
