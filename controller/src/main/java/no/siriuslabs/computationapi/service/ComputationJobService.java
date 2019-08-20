package no.siriuslabs.computationapi.service;

import no.siriuslabs.computationapi.api.model.computation.DomainType;
import no.siriuslabs.computationapi.api.model.computation.WorkPackage;
import no.siriuslabs.computationapi.api.model.computation.WorkPackageResult;
import no.siriuslabs.computationapi.api.model.request.ComputationRequest;
import no.siriuslabs.computationapi.controller.ControllerHelper;
import no.siriuslabs.computationapi.event.ResultUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ComputationJobService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ComputationJobService.class);

	public static final String SERVICE_PATH = "/runComputation";

	private final NodeRegistry nodeRegistry;
	private final RestTemplate restTemplate;
	private final ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	public ComputationJobService(NodeRegistry nodeRegistry/*, RestTemplate restTemplate*/, ApplicationEventPublisher applicationEventPublisher) {
		this.nodeRegistry = nodeRegistry;
//		this.restTemplate = restTemplate; // TODO RestTemplate causes cyclic dependency in Spring - check how to test without injecting...
		restTemplate = new RestTemplate();
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Async
	public void runComputation(String nodeId, URI nodeUri, WorkPackage workPackage) throws URISyntaxException {
		LOGGER.info("Executing asynchronously in thread {}", Thread.currentThread().getName());

		nodeRegistry.occupyNode(nodeId);

		URI uri = new URI(nodeUri + SERVICE_PATH);
		HttpEntity<ComputationRequest> entity = (HttpEntity<ComputationRequest>) ControllerHelper.createHttpEntity(workPackage);

		LOGGER.info("Service to be called @ {} with parameters: {}", uri, workPackage);

		ResponseEntity<Object> response = restTemplate.exchange(uri, HttpMethod.POST, entity, Object.class);

		HttpStatus statusCode = response.getStatusCode();
		LOGGER.info("Service call result=" + statusCode);

		nodeRegistry.freeNode(nodeId);

		WorkPackageResult result = getResultFromResponse(response);

		ResultUpdateEvent event = new ResultUpdateEvent(this, result);
		LOGGER.info("Publishing event: {}", event);
		applicationEventPublisher.publishEvent(event);

		LOGGER.info("Asynchronous execution finished");
	}

	private WorkPackageResult getResultFromResponse(ResponseEntity<Object> response) {
		Map<String, Object> resultMap = (Map<String, Object>) response.getBody();

		Map<String, Object> wpMap = (Map<String, Object>) resultMap.get("workPackage");
		WorkPackage wp = new WorkPackage(DomainType.valueOf((String) wpMap.get("domain")), ((Number) wpMap.get("id")).longValue());
		wp.setRunId((Integer) wpMap.get("runId"));
		wp.setData((Map<String, Object>) wpMap.get("data"));

		final WorkPackageResult result = new WorkPackageResult(wp);
		Map<String, Object> data = (Map<String, Object>) resultMap.get("data");
		result.setData(data);

		return result;
	}

}
