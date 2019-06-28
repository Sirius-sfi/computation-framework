package no.siriuslabs.computationapi.service;

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
		if(!hasNode(node)) {
			node.setStatus(NodeStatus.READY);
			workerNodes.put(node.getId(), node);

			LOGGER.info("Node registered as {} and status changed to {}", node.getId(), NodeStatus.READY);
		}
		else {
			LOGGER.info("Node {} already registered: {}", node, workerNodes.get(node.getId()));
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

	public String reserveNode() {
		WorkerNode freeNode = workerNodes.values().stream().filter((WorkerNode node) -> NodeStatus.READY == node.getStatus()).findFirst().orElse(null);

		if(freeNode == null) {
			return null;
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
			LOGGER.error("Node with ID {} has status {} instead of RESERVED", nodeId, node.getStatus());
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
			LOGGER.error("Node with ID {} has status {} instead of BUSY", nodeId, node.getStatus());
			throw new IllegalStateException("Node with ID " + nodeId + " has status " + node.getStatus() + " instead of BUSY");
		}

		node.setStatus(NodeStatus.READY);
		LOGGER.info("Node {} status changed to {}", node.getId(), node.getStatus());
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
