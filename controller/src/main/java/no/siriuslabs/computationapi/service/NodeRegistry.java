package no.siriuslabs.computationapi.service;

import no.siriuslabs.computationapi.api.model.computation.DomainType;
import no.siriuslabs.computationapi.api.model.node.NodeStatus;
import no.siriuslabs.computationapi.api.model.node.WorkerNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;

// TODO rework to use events where possible?
@Component
public class NodeRegistry {

	private static final Logger LOGGER = LoggerFactory.getLogger(NodeRegistry.class);

	private ConcurrentHashMap<String, WorkerNode> workerNodes = new ConcurrentHashMap<>(5);

	// TODO rework for multi-domains later
	// TODO offer service to set/add/remove domains?
	private DomainType domain;

	public boolean hasNodes() {
		return !workerNodes.isEmpty();
	}

	public int getNumberOfNodes() {
		return workerNodes.size();
	}

	public boolean hasNode(WorkerNode node) {
		return hasNode(node.getId());
	}

	public boolean hasNode(String nodeId) {
		return workerNodes.containsKey(nodeId);
	}

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

	public void unregisterNode(String id) {
		if(hasNode(id)) {
			workerNodes.remove(id);
		}
		else {
			LOGGER.info("Node with ID {} was not found and could not be removed", id);
		}
	}

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

	public URI getUriForNode(String nodeId) {
		if(!hasNode(nodeId)) {
			LOGGER.info("Node with ID {} not found", nodeId);
			return null;
		}

		WorkerNode node = workerNodes.get(nodeId);
		return node.getUri();
	}

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

	public synchronized DomainType getDomain() {
		return domain;
	}

	public synchronized void setDomain(DomainType domain) {
		this.domain = domain;
	}

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
