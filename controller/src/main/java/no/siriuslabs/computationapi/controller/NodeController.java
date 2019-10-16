package no.siriuslabs.computationapi.controller;

import no.siriuslabs.computationapi.api.exception.InvalidParameterException;
import no.siriuslabs.computationapi.api.model.node.WorkerNode;
import no.siriuslabs.computationapi.config.ControllerProperties;
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
public class NodeController extends AbstractController {

	private static final Logger LOGGER = LoggerFactory.getLogger(NodeController.class);

	@Autowired
	public NodeController(NodeRegistry nodeRegistry, ControllerProperties controllerProperties) {
		super(nodeRegistry, controllerProperties);
	}

	@GetMapping("/numberOfNodes")
	public int getNumberOfNodes() {
		final String methodName = "getNumberOfNodes";
		logRequestStart(LOGGER, methodName);

		final int result = getNodeRegistry().getNumberOfNodes();

		logRequestFinish(LOGGER, methodName, result);
		return result;
	}

	@GetMapping("/getNodeList")
	public String getNodeList() {
		final String methodName = "getNodeList";
		logRequestStart(LOGGER, methodName);

		final String result = getNodeRegistry().getNodeList();

		logRequestFinish(LOGGER, methodName, result);
		return result;
	}

	@PostMapping("/registerNode")
	public ResponseEntity<Object> registerNode(@RequestBody WorkerNode node) {
		final String methodName = "registerNode";
		logRequestStart(LOGGER, methodName, node);

		if(getNodeRegistry().hasNode(node)) {
			LOGGER.info("Node with ID {} already registered", node.getId());
			final ResponseEntity<Object> response = ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Node with id " + node.getId() + " already registered");
			logRequestFinish(LOGGER, methodName, response, node);
			return response;
		}

		getNodeRegistry().registerNode(node);

		logRequestFinish(LOGGER, methodName, node, node);

		return ResponseEntity.ok(node);
	}

	@PostMapping("/unregisterNode/{id}")
	public void unregisterNode(@PathVariable String id) {
		final String methodName = "unregisterNode";
		logRequestStart(LOGGER, methodName, id);

		if(!getNodeRegistry().hasNode(id)) {
			throw new InvalidParameterException("Node with id " + id + " unknown");
		}

		getNodeRegistry().unregisterNode(id);

		logVoidRequestFinish(LOGGER, methodName, id);
	}

	public void pingNodes() {
		if(getNodeRegistry().hasNodes()) {
			getNodeRegistry().pingAllNodes();
		}
	}

	// TODO add timer method to mark or clean up nodes that are
		// - RESERVED for too long
		// - SUSPICIOUS
		// - DONE (not sure yet about this state)

}
