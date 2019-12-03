package no.siriuslabs.computationapi.service;

import no.siriuslabs.computationapi.api.model.computation.DomainType;
import no.siriuslabs.computationapi.api.model.node.NodeStatus;
import no.siriuslabs.computationapi.api.model.node.WorkerNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry that keeps track of the identities and states of all worker nodes know to the controller and of the currently active DomainType the controller serves.<p>
 * The class has several methods that provide information about nodes and domain as well as those to register or de-register, reserve, occupy and free worker nodes.
 */
// TODO rework to use events where possible?
@Component
public class NodeRegistry {

	private static final Logger LOGGER = LoggerFactory.getLogger(NodeRegistry.class);

	/**
	 * Map of WorkerNodes know in the system, identified by their node-ID.
	 */
	private ConcurrentHashMap<String, WorkerNode> workerNodes = new ConcurrentHashMap<>(5);

	/**
	 * Currently active DomainType in the system.
	 */
	// TODO rework for multi-domains later
	// TODO offer service to set/add/remove domains?
	private DomainType domain;

	/**
	 * Returns true if any WorkerNodes are registered in the system, otherwise false.
	 */
	public boolean hasNodes() {
		return !workerNodes.isEmpty();
	}

	/**
	 * Returns the number of WorkerNodes currently registered in the system.
	 */
	public int getNumberOfNodes() {
		return workerNodes.size();
	}

	/**
	 * Returns true if this WorkerNode is registered in the system, otherwise returns false.
	 */
	public boolean hasNode(WorkerNode node) {
		return hasNode(node.getId());
	}

	/**
	 * Returns true if a WorkerNode with the given ID is registered in the system, otherwise returns false.
	 */
	public boolean hasNode(String nodeId) {
		return workerNodes.containsKey(nodeId);
	}

	/**
	 * Returns a String representation of a list of all WorkerNodes registered in the system.
	 */
	public String getNodeList() {
		StringBuilder nodes = new StringBuilder(200);
		nodes.append("Nodes: ");
		nodes.append(System.lineSeparator());

		for(String nodeName : workerNodes.keySet()) {
			nodes.append(nodeName);
			nodes.append(" --> ");
			nodes.append(workerNodes.get(nodeName));
			nodes.append(System.lineSeparator());
		}

		return nodes.toString();
	}

	/**
	 * Registers the given WorkerNode as an available node with the controller. After registration the node's status will be set to READY and the nodes will be eligible to receive tasks to execute.<p>
	 * Should this node or a node carrying the same node ID already be registered, the node will <b>not</b> be registered.<p>
	 * If there should not yet be an active DomainType set for the controller, this node's DomainType will become the active one on registration.<p>
	 * Should this node's DomainType not match the already set type of the controller, an IllegalArgumentException will be thrown.
	 */
	public void registerNode(WorkerNode node) {
		LOGGER.info("Register node {}", node);
		if(hasNode(node)) {
			LOGGER.info("Node {} already registered: {}", node, workerNodes.get(node.getId()));
		}
		else {
			if(getDomain() == null) {
				LOGGER.info("Domain not set - accepting node's domain {}", node.getDomainType());
				setDomain(node.getDomainType());
			}
			else if(!getDomain().getDomainType().equals(node.getDomainType().getDomainType())) {
				final String message = "Node " + node.getId() + " has set a domain type different from the controller (" + node.getDomainType() + " <> " + getDomain() + ')';
				LOGGER.info(message);
				throw new IllegalArgumentException(message);
			}

			node.setStatus(NodeStatus.READY);
			workerNodes.put(node.getId(), node);

			LOGGER.info("Node registered as {} and status changed to {}", node.getId(), NodeStatus.READY);
		}
	}

	/**
	 * Unregisters the WorkerNode with the given ID from the controller. The node will not be given any new tasks after de-registration.<p>
	 * Should the given node ID not belong to a registered node nothing will be done.
	 */
	public void unregisterNode(String id) {
		if(hasNode(id)) {
			workerNodes.remove(id);
		}
		else {
			LOGGER.info("Node with ID {} was not found and could not be removed", id);
		}
	}

	/**
	 * Tries to reserve a WorkerNode for use with the given domain type.<p>
	 * Reserving a node blocks it for other domains and tasks. It can be given something to do after a successful reservation or returned to the pool without being used.<p>
	 * The method searches for the first free node that is capable to run tasks for the given domain type.
	 * If a node is available, the method changes its status to RESERVED and returns the assigned node's ID.
	 * If no compatible node is available null will be returned instead of a node ID.
	 */
	public String reserveNode(DomainType domainType) {
		WorkerNode freeNode = workerNodes.values().stream().filter((WorkerNode node)
				-> (NodeStatus.READY == node.getStatus()) && (node.getDomainType().getDomainType().equals(domainType.getDomainType()))).findFirst().orElse(null);

		if(freeNode == null) {
			return null;
		}

		if(NodeStatus.BUSY == freeNode.getStatus() || NodeStatus.RESERVED == freeNode.getStatus()) {
			LOGGER.error("Unexpected node status on reserve: Node with ID {} has status {} and cannot be reserved", freeNode.getId(), freeNode.getStatus());
			throw new IllegalStateException("Node with ID " + freeNode.getId() + " has status " + freeNode.getStatus() + " and cannot be reserved");
		}
		if(NodeStatus.READY != freeNode.getStatus()) {
			LOGGER.warn("Unexpected node status on reserve: Node with ID {} has status {} instead of READY", freeNode.getId(), freeNode.getStatus());
		}

		freeNode.setStatus(NodeStatus.RESERVED);
		return freeNode.getId();
	}

	/**
	 * Returns the URI encapsulated in the WorkerNode identified by the given nodeId. Should no WorkerNode with this ID exist null will be returned.
	 */
	public URI getUriForNode(String nodeId) {
		if(!hasNode(nodeId)) {
			LOGGER.info("Node with ID {} not found", nodeId);
			return null; // TODO use IllegalArgumentException instead?
		}

		WorkerNode node = workerNodes.get(nodeId);
		return node.getUri();
	}

	/**
	 * Registers the WorkerNode with the given nodeId as occupied with actively performing a task by changing its status from RESERVED to BUSY.<p>
	 * Should no WorkerNode with this ID exist a IllegalArgumentException will be thrown as this prevents a task from being started.<p>
	 * Should the WorkerNode have a different status than RESERVED an IllegalStateException will be thrown as this prevents a task from being started.
	 */
	public void occupyNode(String nodeId) {
		if(!hasNode(nodeId)) {
			LOGGER.error("Node with ID {} unknown", nodeId);
			throw new IllegalArgumentException("Node with ID " + nodeId + " unknown");
		}

		WorkerNode node = workerNodes.get(nodeId);
		if(NodeStatus.RESERVED != node.getStatus()) {
			LOGGER.error("Unexpected node status on occupy: Node with ID {} has status {} instead of RESERVED", nodeId, node.getStatus());
			throw new IllegalStateException("Node with ID " + nodeId + " has status " + node.getStatus() + " instead of RESERVED");
		}

		node.setStatus(NodeStatus.BUSY);
		LOGGER.info("Node {} status changed to {}", node.getId(), node.getStatus());
	}

	/**
	 * Registers the WorkerNode with the given nodeId as no longer occupied with actively performing a task and free for reservation again by changing its status from BUSY to READY.<p>
	 * Should no WorkerNode with this ID exist a IllegalArgumentException will be thrown as this prevents the correct node from being used again.
	 */
	public void freeNode(String nodeId) {
		if(!hasNode(nodeId)) {
			LOGGER.error("Node with ID {} unknown", nodeId);
			throw new IllegalArgumentException("Node with ID " + nodeId + " unknown");
		}

		WorkerNode node = workerNodes.get(nodeId);
		if(NodeStatus.BUSY != node.getStatus()) {
			LOGGER.warn("Unexpected node status on free: Node with ID {} has status {} instead of BUSY", nodeId, node.getStatus());
		}

		node.setStatus(NodeStatus.READY);
		LOGGER.info("Node {} status changed to {}", node.getId(), node.getStatus());
	}

	/**
	 * Returns the currently active DomainType in the system or null if there is no active domain set yet.
	 */
	public synchronized DomainType getDomain() {
		return domain;
	}

	/**
	 * Set the system's currently active DomainType to the given one.
	 */
	// TODO check for overwrites!?
	public synchronized void setDomain(DomainType domain) {
		this.domain = domain;
	}

	/**
	 * Triggers a ping of all registered WorkerNodes. Called from a timer regularly.
	 */
	public void pingAllNodes() {
		for(WorkerNode node : workerNodes.values()) {
			if(NodeStatus.UNAVAILABLE != node.getStatus()) {
				// TODO use RestTemplate to ping
				// TODO use extra threads to avoid ping hanging!
				// TODO if no reply do something like mark suspicious
			}
		}
	}

}
