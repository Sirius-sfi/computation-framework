package no.siriuslabs.computationapi.service;

import no.siriuslabs.computationapi.ControllerApplication;
import no.siriuslabs.computationapi.api.model.computation.DomainType;
import no.siriuslabs.computationapi.api.model.node.NodeStatus;
import no.siriuslabs.computationapi.api.model.node.WorkerNode;
import no.siriuslabs.computationapi.model.TestDomainType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ControllerApplication.class)
@ActiveProfiles("test")
public class NodeRegistryTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(NodeRegistryTest.class);

	@DisplayName("Test status of registry after initialization")
	@Test
	public void testAfterInitialization() {
		NodeRegistry nodeRegistry = new NodeRegistry();

		assertFalse(nodeRegistry.hasNodes(), "NodeRegistry must not have any registered nodes after initialization");
		assertEquals(0, nodeRegistry.getNumberOfNodes(), "NodeRegistry must not have any registered nodes after initialization");

		WorkerNode node = createWorkerNode("someID");

		assertFalse(nodeRegistry.hasNode(node), "NodeRegistry must not have any registered nodes after initialization");
	}

	@DisplayName("Test adding and re-adding nodes to the registry")
	@Test
	public void testRegisterNode() {
		NodeRegistry nodeRegistry = new NodeRegistry();
		WorkerNode node = createWorkerNode("someID");


		// add node
		nodeRegistry.registerNode(node);

		assertEquals(1, nodeRegistry.getNumberOfNodes(), "WorkerNode was not added to registry as expected");
		assertTrue(nodeRegistry.hasNode(node.getId()), "WorkerNode was not added to registry as expected");


		// try to add same node again - number of nodes should not change
		nodeRegistry.registerNode(node);
		assertEquals(1, nodeRegistry.getNumberOfNodes(), "Re-registering an existing node must not change the registry");
		assertTrue(nodeRegistry.hasNode(node.getId()), "Re-registering an existing node must not change the registry");


		// try to add another node with an existing ID - number of nodes should not change
		WorkerNode sameIdNode = createWorkerNode("someID");

		assertEquals(1, nodeRegistry.getNumberOfNodes(), "Re-registering an existing ID must not change the registry");
		assertTrue(nodeRegistry.hasNode(node.getId()), "Re-registering an existing ID must not change the registry");
		assertTrue(nodeRegistry.hasNode(sameIdNode.getId()), "Re-registering an existing ID must not change the registry");


		// add a node with a different ID
		WorkerNode anotherIdNode = createWorkerNode("someOtherID");

		nodeRegistry.registerNode(anotherIdNode);

		assertEquals(2, nodeRegistry.getNumberOfNodes(), "2nd WorkerNode was not added to registry as expected");
		assertTrue(nodeRegistry.hasNode(anotherIdNode.getId()), "2nd WorkerNode was not added to registry as expected");
		assertTrue(nodeRegistry.hasNode(node.getId()), "Adding 2nd WorkerNode must not remove existing nodes");
	}

	@DisplayName("Test registering nodes in relation to the set domain")
	@Test
	public void testRegisterNode_Domain() {
		// try to add a node with the 'wrong' domain for a registry
		NodeRegistry domainNodeRegistry = new NodeRegistry();
		domainNodeRegistry.setDomain(TestDomainType.TEST_2);

		WorkerNode node = createWorkerNode("someID");

		assertThrows(IllegalArgumentException.class, () -> { domainNodeRegistry.registerNode(node); });


		// make sure a registry is initialized without a domain set
		NodeRegistry nodeRegistry = new NodeRegistry();
		assertNull(nodeRegistry.getDomain());

		// if no domain is set yet, the domain from first node registered is set as active domain
		nodeRegistry.registerNode(node);
		assertEquals(TestDomainType.TEST_1, nodeRegistry.getDomain(), "Domain of domain-less NodeRegistry did not change to first node's set domain");

		WorkerNode anotherIdNode = createWorkerNode("someOtherID");

		nodeRegistry.registerNode(anotherIdNode);
		assertEquals(2, nodeRegistry.getNumberOfNodes());
	}

	@DisplayName("Test removing a node from the empty registry")
	@Test
	public void testUnregisterNode_Empty() {
		NodeRegistry nodeRegistry = new NodeRegistry();
		WorkerNode node = createWorkerNode("someID");

		// try to remove node from empty registry
		assertFalse(nodeRegistry.hasNodes(), "There must not be any nodes in the registry at this point");
		nodeRegistry.unregisterNode(node.getId());
		assertFalse(nodeRegistry.hasNodes(), "There must not be any nodes in the registry at this point");
	}

	@DisplayName("Test removing existing and non-existing nodes from the registry")
	@Test
	public void testUnregisterNode() {
		NodeRegistry nodeRegistry = new NodeRegistry();

		WorkerNode node = createWorkerNode("someID");
		WorkerNode anotherNode = createWorkerNode("someOtherID");


		// try to remove an existing node from the non-empty registry
		nodeRegistry.registerNode(node);
		nodeRegistry.registerNode(anotherNode);

		assertEquals(2, nodeRegistry.getNumberOfNodes(), "Expected number of 2 nodes not given");
		nodeRegistry.unregisterNode(node.getId());

		assertEquals(1, nodeRegistry.getNumberOfNodes(), "Node was not removed as expected");
		assertTrue(nodeRegistry.hasNode(anotherNode), "Node supposed to remain in the registry was removed");
		assertFalse(nodeRegistry.hasNode(node), "Node supposed to have been removed is still in the registry");


		// try to remove a non-registered ID from the registry
		nodeRegistry.unregisterNode("nonExistingID");
		assertEquals(1, nodeRegistry.getNumberOfNodes(), "Node was not removed as expected");
		assertTrue(nodeRegistry.hasNode(anotherNode), "Node supposed to remain in the registry was removed");


		// try to remove the last existing node from the registry
		nodeRegistry.unregisterNode(anotherNode.getId());
		assertFalse(nodeRegistry.hasNode(anotherNode), "Node supposed to have been removed is still in the registry");
		assertFalse(nodeRegistry.hasNodes(), "Registry is expected to be empty by now");
	}

	@DisplayName("Test reserving nodes through multiple steps")
	@Test
	public void testReserveNode() {
		NodeRegistry nodeRegistry = new NodeRegistry();

		WorkerNode busyNode = createWorkerNode("busyNode");
		nodeRegistry.registerNode(busyNode);
		busyNode.setStatus(NodeStatus.BUSY); // status has to be set AFTER registering, as during that process the status is set to READY!

		WorkerNode reservedNode = createWorkerNode("reservedNode");
		nodeRegistry.registerNode(reservedNode);
		reservedNode.setStatus(NodeStatus.RESERVED); // status has to be set AFTER registering, as during that process the status is set to READY!

		WorkerNode readyNode1 = createWorkerNode("readyNode1");
		nodeRegistry.registerNode(readyNode1);
		readyNode1.setStatus(NodeStatus.READY);

		WorkerNode readyNode2 = createWorkerNode("readyNode2");
		nodeRegistry.registerNode(readyNode2);
		readyNode2.setStatus(NodeStatus.READY);


		// reserve a node - it should be one of the two "ready nodes"
		String reservedID1 = nodeRegistry.reserveNode(TestDomainType.TEST_1);
		assertTrue(readyNode1.getId().equals(reservedID1) || readyNode2.getId().equals(reservedID1), "ID of reserved node not in expected range");


		// reserve another node - it should be the remaining one of the "ready nodes"
		String reservedID2 = nodeRegistry.reserveNode(TestDomainType.TEST_1);
		assertTrue(readyNode1.getId().equals(reservedID2) || readyNode2.getId().equals(reservedID2), "ID of reserved node not in expected range");
		assertNotEquals(reservedID1, reservedID2, "The ID of an already reserved node must not be chosen again");


		// try to reserve a 3rd time - there are no ready nodes left, so we should not get any node back
		String reservedID3 = nodeRegistry.reserveNode(TestDomainType.TEST_1);
		assertNull(reservedID3, "A node was reserved even though there cannot be any free nodes left");
	}

	@DisplayName("Test if reserving nodes works correctly for given domains")
	@Test
	public void testReserveNodeByDomain() {
		NodeRegistry nodeRegistry = new NodeRegistry();

		// disabled - makes no sense anymore until multi-domain is supported due to domain-protection in registration logic
//		WorkerNode otherDomainNode = createWorkerNode("otherDomain");
//		otherDomainNode.setDomainType(TestDomainType); // TODO replace by better test domain type
//		nodeRegistry.registerNode(otherDomainNode);
//		otherDomainNode.setStatus(NodeStatus.READY);

		WorkerNode rightDomainNode = createWorkerNode("rightDomain");
		nodeRegistry.registerNode(rightDomainNode);
		rightDomainNode.setStatus(NodeStatus.BUSY);


		String firstTry = nodeRegistry.reserveNode(TestDomainType.TEST_1);
		assertNull(firstTry, "Node was reserved even though no node of correct domain was available");

		nodeRegistry.freeNode(rightDomainNode.getId());

		String secondTry = nodeRegistry.reserveNode(TestDomainType.TEST_1);
		assertEquals(rightDomainNode.getId(), secondTry, "Wrong or no node was reserved, even though only one matching node was available");
	}

	@DisplayName("Test retrieving the URI property of existing and non-existing nodes")
	@Test
	public void testGetUriForNode() {
		NodeRegistry nodeRegistry = new NodeRegistry();

		WorkerNode node = createWorkerNode("someID");
		try {
			node.setUri(new URI("http://sirius-labs.no"));
		}
		catch(URISyntaxException e) {
			Assertions.fail(e.getMessage());
			LOGGER.error(e.getMessage(), e);
		}

		nodeRegistry.registerNode(node);

		// try to get URI for non-existing node
		URI uri = nodeRegistry.getUriForNode("notThere");
		assertNull(uri, "URI expected to be null for non-existing node");

		// try to get URI for existing node
		uri = nodeRegistry.getUriForNode(node.getId());
		assertEquals(node.getUri(), uri, "Retrieved URI must match URI of registered node");
	}

	private WorkerNode createWorkerNode(String id) {
		WorkerNode node = new WorkerNode();
		node.setId(id);
		node.setDomainType(TestDomainType.TEST_1);
		return node;
	}

}
