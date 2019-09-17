package no.siriuslabs.computationapi.service;

import no.siriuslabs.computationapi.api.model.computation.DomainType;
import no.siriuslabs.computationapi.api.model.computation.WorkPackage;
import no.siriuslabs.computationapi.api.model.request.ComputationRequest;
import no.siriuslabs.computationapi.controller.ControllerHelper;
import no.siriuslabs.computationapi.event.DataPreparartionFinishedEvent;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class DataPreparationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DataPreparationService.class);

	public static final String SERVICE_PATH = "/prepareAndPackageData";

	private final NodeRegistry nodeRegistry;
	private final RestTemplate restTemplate;
	private final ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	public DataPreparationService(NodeRegistry nodeRegistry, RestTemplate restTemplate, ApplicationEventPublisher applicationEventPublisher) {
		this.nodeRegistry = nodeRegistry;
		this.restTemplate = restTemplate;
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Async
	public void prepareAndPackageData(String nodeId, URI nodeUri, ComputationRequest request) throws URISyntaxException, ExecutionException {
		LOGGER.info("Executing asynchronously in thread {}", Thread.currentThread().getName());

		long starttime = System.currentTimeMillis();

		nodeRegistry.occupyNode(nodeId);

		URI uri = new URI(nodeUri + SERVICE_PATH);
		HttpEntity<ComputationRequest> entity = (HttpEntity<ComputationRequest>) ControllerHelper.createHttpEntity(request);

		LOGGER.info("Service to be called @ {} with parameters: {}", uri, request);

		ResponseEntity<Object> response = restTemplate.exchange(uri, HttpMethod.POST, entity, Object.class);

		HttpStatus statusCode = response.getStatusCode();
		LOGGER.info("Service call result=" + statusCode);

		List<Map<String, Object>> result = (List<Map<String, Object>>) response.getBody();

		nodeRegistry.freeNode(nodeId);

		List<WorkPackage> workPackages = new ArrayList<>(result.size());
		for(Map<String, Object> row : result) {
			final DomainType domain = DomainType.valueOf((String) row.get("domain"));
			final Number id = (Number) row.get("id");
			WorkPackage workPackage = new WorkPackage(domain, id.longValue());
			workPackage.setRunId((Integer) row.get("runId"));
			workPackage.setData((Map<String, Object>) row.get("data"));
			workPackages.add(workPackage);
		}

		long finishtime = System.currentTimeMillis();
		request.setPreparationTime(finishtime - starttime);
		LOGGER.info("Preparartion phase on node {} took {} ms", nodeId, finishtime - starttime);

		DataPreparartionFinishedEvent event = new DataPreparartionFinishedEvent(this, request, workPackages);
		LOGGER.info("Publishing event: {}", event);

		applicationEventPublisher.publishEvent(event);

		LOGGER.info("Asynchronous execution finished");
	}

}
