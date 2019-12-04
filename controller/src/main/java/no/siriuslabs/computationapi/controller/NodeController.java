package no.siriuslabs.computationapi.controller;

import no.siriuslabs.computationapi.api.exception.InvalidParameterException;
import no.siriuslabs.computationapi.api.model.computation.DomainType;
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

/**
 * Rest controller responsible for worker node and domain type related services.<p>
 * It offers several service methods that provide information about the registered nodes and the active domain of the controller.
 * It also has two crucial service methods for registering and unregistering worker nodes with the controller.
 */
@RestController
public class NodeController extends AbstractController {

	private static final Logger LOGGER = LoggerFactory.getLogger(NodeController.class);

	/**
	 * Autowired constructor.
	 */
	@Autowired
	public NodeController(NodeRegistry nodeRegistry, ControllerProperties controllerProperties) {
		super(nodeRegistry, controllerProperties);
	}

	/**
	 * Returns the number of worker nodes of any domain type currently registered with the controller.
	 */
	@GetMapping("/numberOfNodes")
	public int getNumberOfNodes() {
		final String methodName = "getNumberOfNodes";
		logRequestStart(LOGGER, methodName);

		final int result = getNodeRegistry().getNumberOfNodes();

		logRequestFinish(LOGGER, methodName, result);
		return result;
	}

	/**
	 * Returns a String with a list of all worker nodes of any domain type currently registered with the controller.
	 */
	@GetMapping("/getNodeList")
	public String getNodeList() {
		final String methodName = "getNodeList";
		logRequestStart(LOGGER, methodName);

		final String result = getNodeRegistry().getNodeList();

		logRequestFinish(LOGGER, methodName, result);
		return result;
	}

	/**
	 * Registers the given WorkerNode as an available node with the controller. After registration the node is eligible to receive tasks to execute.<p>
	 * Should this node or a node carrying the same node ID already be registered, the node will <b>not</b> be registered and this method will return HttpStatus 406 - "Not acceptable".
	 * On successful registration it will return the node itself.<p>
	 * If there should not yet be an active DomainType set for the controller, this node's DomainType will become the active one on registration.<p>
	 * Should this node's DomainType not match the already set type of the controller, the node will not be registered and the method will return HttpStatus 406 - "Not acceptable" with an appropriate message.
	 */
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

		if(getNodeRegistry().hasDomainSet() && !getNodeRegistry().getDomain().getDomainType().equals(node.getDomainType().getDomainType())) {
			final String message = "Node " + node.getId() + " has set a domain type different from the controller (" + node.getDomainType() + " <> " + getNodeRegistry().getDomain().getDomainType() + ')';
			LOGGER.info(message);
			final ResponseEntity<Object> response = ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(message);
			logRequestFinish(LOGGER, methodName, response, node);
			return response;
		}

		getNodeRegistry().registerNode(node);

		logRequestFinish(LOGGER, methodName, node, node);

		return ResponseEntity.ok(node);
	}

	/**
	 * Unregisters the WorkerNode with the given ID from the controller. The node will not be given any new tasks after de-registration.<p>
	 * Should the node deliver a result after being unregistered, the result will be accepted anyway. If the node shuts down before a result is delivered, the work will be reassigned later.<p>
	 * Should the given node ID not belong to a registered node an error will be thrown.
	 */
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

	/**
	 * Returns the currently active domain type on the controller. If there is no active domain type, the result will be empty.
	 */
	@GetMapping("activeDomain")
	public String getActiveDomain() {
		final String methodName = "getNodeList";
		logRequestStart(LOGGER, methodName);

		final DomainType domain = getNodeRegistry().getDomain();
		String result = domain == null ? null : domain.getDomainType();

		logRequestFinish(LOGGER, methodName, result);
		return result;
	}

	/**
	 * Sets the active domain type to the given one.<p>
	 * Throws an IllegalStateException if an already set active DomainType would be overwritten by the new one.
	 */
	public void setDomain(DomainType domain) {
		getNodeRegistry().setDomain(domain);
	}

	/**
	 * Triggers pinging of all registered worker nodes.
	 */
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
