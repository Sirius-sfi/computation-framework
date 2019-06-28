package no.siriuslabs.computationapi.controller;

import no.siriuslabs.computationapi.api.exception.InvalidParameterException;
import no.siriuslabs.computationapi.api.model.node.WorkerNode;
import no.siriuslabs.computationapi.service.NodeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NodeController {

	private static final Logger LOGGER = LoggerFactory.getLogger(NodeController.class);

	private final NodeRegistry nodeRegistry;

	@Autowired
	public NodeController(NodeRegistry nodeRegistry) {
		this.nodeRegistry = nodeRegistry;
	}

	@GetMapping("/numberOfNodes")
	public int getNumberOfNodes() {
		final String methodName = "getNumberOfNodes";
		ControllerHelper.logRequestStart(LOGGER, methodName);

		final int result = nodeRegistry.getNumberOfNodes();

		ControllerHelper.logRequestFinish(LOGGER, methodName, result);
		return result;
	}

	@GetMapping("/getNodeList")
	public String getNodeList() {
		final String methodName = "getNodeList";
		ControllerHelper.logRequestStart(LOGGER, methodName);

		final String result = nodeRegistry.getNodeList();

		ControllerHelper.logRequestFinish(LOGGER, methodName, result);
		return result;
	}

	@PostMapping("/registerNode")
	public ResponseEntity<Object> registerNode(@RequestBody WorkerNode node) {
		final String methodName = "registerNode";
		ControllerHelper.logRequestStart(LOGGER, methodName, node);

		if(nodeRegistry.hasNode(node)) {
			LOGGER.info("Node with ID {} already registered", node.getId());
			final ResponseEntity<Object> response = ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Node with id " + node.getId() + " already registered");
			ControllerHelper.logRequestFinish(LOGGER, methodName, response, node);
			return response;
		}

		nodeRegistry.registerNode(node);

		ControllerHelper.logRequestFinish(LOGGER, methodName, node, node);

		return ResponseEntity.ok(node);
	}

	@PostMapping("/unregisterNode/{id}")
	public void unregisterNode(@PathVariable String id) {
		final String methodName = "unregisterNode";
		ControllerHelper.logRequestStart(LOGGER, methodName, id);

		if(!nodeRegistry.hasNode(id)) {
			throw new InvalidParameterException("Node with id " + id + " unknown");
		}

		nodeRegistry.unregisterNode(id);

		ControllerHelper.logVoidRequestFinish(LOGGER, methodName, id);
	}

	public void pingNodes() {
		if(nodeRegistry.hasNodes()) {
			nodeRegistry.pingAllNodes();
		}
	}

	// TODO add timer method to mark or clean up nodes that are
		// - RESERVED for too long
		// - SUSPICIOUS
		// - DONE (not sure yet about this state)

}
