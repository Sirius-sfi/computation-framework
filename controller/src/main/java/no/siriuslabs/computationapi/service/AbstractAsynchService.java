package no.siriuslabs.computationapi.service;

import no.siriuslabs.computationapi.api.model.request.ComputationRequest;
import no.siriuslabs.computationapi.controller.ControllerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Common superclass of Spring services. Provides some functionality such as calling a webservice on a worker node and some shared injections.
 */
public abstract class AbstractAsynchService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAsynchService.class);

	/**
	 * Reference to the NodeRegistry to be able to reserve and free nodes.
	 */
	private final NodeRegistry nodeRegistry;
	/**
	 * Spring RestTemplate used to call worker node services.
	 */
	private final RestTemplate restTemplate;
	/**
	 * Event publisher to pass updates to other application parts.
	 */
	private final ApplicationEventPublisher applicationEventPublisher;

	/**
	 * Constructor accepting the NodeRegistry and the ApplicationEventPublisher (to be injected into the concrete implementation class).
	 */
	protected AbstractAsynchService(NodeRegistry nodeRegistry, ApplicationEventPublisher applicationEventPublisher) {
		this.nodeRegistry = nodeRegistry;
		this.applicationEventPublisher = applicationEventPublisher;
		restTemplate = new RestTemplate();	// TODO injected RestTemplate causes cyclic dependency in Spring
	}

	/**
	 * Calls a Rest service on a worker node corresponding to the given parameters.
	 * @param nodeId 	Identifier of the node that is to be called.
	 * @param nodeUri 	URI of the <b>node</b> to be called (not the complete service-URI).
	 * @param parameter	Parameter to be passed in the call (target service must use a RequestBody parameter type).
	 * @return The original ResponseEntity returned by the service called.
	 * @throws URISyntaxException if the parameter nodeUri and the result of getServicePath() should not combine to a valid URI.
	 */
	protected ResponseEntity<Object> callNodeWebservice(String nodeId, URI nodeUri, Object parameter) throws URISyntaxException {
		nodeRegistry.occupyNode(nodeId);

		URI uri = new URI(nodeUri + getServicePath());
		HttpEntity<ComputationRequest> entity = (HttpEntity<ComputationRequest>) ControllerHelper.createHttpEntity(parameter);

		LOGGER.info("Service to be called @ {} with parameters: {}", uri, parameter);

		ResponseEntity<Object> response = restTemplate.exchange(uri, HttpMethod.POST, entity, Object.class);

		HttpStatus statusCode = response.getStatusCode();
		LOGGER.info("Service call result={}", statusCode);

		nodeRegistry.freeNode(nodeId);

		return response;
	}

	/**
	 * Returns the relative path of the worker node service to be called.
	 */
	protected abstract String getServicePath();

	protected NodeRegistry getNodeRegistry() {
		return nodeRegistry;
	}

	protected RestTemplate getRestTemplate() {
		return restTemplate;
	}

	protected ApplicationEventPublisher getApplicationEventPublisher() {
		return applicationEventPublisher;
	}
}
