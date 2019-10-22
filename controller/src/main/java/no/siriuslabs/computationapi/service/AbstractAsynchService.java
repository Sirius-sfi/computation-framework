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

public abstract class AbstractAsynchService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAsynchService.class);

	private final NodeRegistry nodeRegistry;
	private final RestTemplate restTemplate;
	private final ApplicationEventPublisher applicationEventPublisher;

	protected AbstractAsynchService(NodeRegistry nodeRegistry, ApplicationEventPublisher applicationEventPublisher) {
		this.nodeRegistry = nodeRegistry;
		this.applicationEventPublisher = applicationEventPublisher;
		restTemplate = new RestTemplate();	// TODO injected RestTemplate causes cyclic dependency in Spring
	}

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
