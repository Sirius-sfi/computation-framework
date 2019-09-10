package no.siriuslabs.computationapi.controller;

import no.siriuslabs.computationapi.api.model.computation.ComputationResult;
import no.siriuslabs.computationapi.api.model.computation.ComputationStatus;
import no.siriuslabs.computationapi.api.model.computation.DomainType;
import no.siriuslabs.computationapi.api.model.computation.RequestProtocol;
import no.siriuslabs.computationapi.api.model.computation.ResultsProtocol;
import no.siriuslabs.computationapi.api.model.computation.Status;
import no.siriuslabs.computationapi.api.model.computation.WorkPackage;
import no.siriuslabs.computationapi.config.ControllerProperties;
import no.siriuslabs.computationapi.event.AbstractDataWorkflowEvent;
import no.siriuslabs.computationapi.event.ComputationRequestAddedEvent;
import no.siriuslabs.computationapi.event.DataPreparartionFinishedEvent;
import no.siriuslabs.computationapi.event.ResultUpdateEvent;
import no.siriuslabs.computationapi.service.NodeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class ResultController implements ApplicationListener<AbstractDataWorkflowEvent> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResultController.class);

	private static final String ACCUMULATE_RESULTS_PATH = "/accumulateResults";

	private final NodeRegistry nodeRegistry;
	private final ControllerProperties controllerProperties;
	private final RestTemplate restTemplate;

	private Map<DomainType, RequestProtocol> protocolMap = new ConcurrentHashMap<>();

	@Autowired
	public ResultController(NodeRegistry nodeRegistry, ControllerProperties controllerProperties, RestTemplate restTemplate) {
		this.nodeRegistry = nodeRegistry;
		this.controllerProperties = controllerProperties;
		this.restTemplate = restTemplate;
	}

	@Override
	public void onApplicationEvent(AbstractDataWorkflowEvent workflowEvent) {
		LOGGER.info("Event coming in");
		if(workflowEvent instanceof ComputationRequestAddedEvent) {
			ComputationRequestAddedEvent event = (ComputationRequestAddedEvent) workflowEvent;
			LOGGER.info("Event is {} --> {}", event.getClass().getName(), event);

			final DomainType domain = event.getComputationRequest().getDomain();
			protocolMap.put(domain, new RequestProtocol(domain, event.getComputationRequest()));
			LOGGER.info("ComputationRequest added to protocol in domain {}", domain);
		}
		else if(workflowEvent instanceof DataPreparartionFinishedEvent) {
			DataPreparartionFinishedEvent event = (DataPreparartionFinishedEvent) workflowEvent;
			LOGGER.info("Event is {} --> {}", event.getClass().getName(), event);

			final DomainType domain = event.getDomain();
			List<WorkPackage> workPackages = event.getWorkPackages();
			protocolMap.get(domain).addWorkPackages(workPackages);
			LOGGER.info("Added {} work packages to protocol in domain {}", workPackages.size(), domain);
		}
		else if(workflowEvent instanceof ResultUpdateEvent) {
			ResultUpdateEvent event = (ResultUpdateEvent) workflowEvent;
			LOGGER.info("Event is {} --> {}", event.getClass().getName(), event);

			final DomainType domain = event.getWorkPackageResult().getWorkPackage().getDomain();
			protocolMap.get(domain).addWorkPackageResults(event.getWorkPackageResult());
			LOGGER.info("Result added for WP {} in domain {}", event.getWorkPackageResult().getWorkPackage().getId(), domain);
		}
		else {
			LOGGER.warn("Unknown even {}", workflowEvent);
		}
	}

	@GetMapping("/status/{domain}")
	public ComputationStatus getStatus(@PathVariable("domain") String domain) {
		final String methodName = "getStatus";
		ControllerHelper.logRequestStart(LOGGER, methodName, domain);

		ControllerHelper.checkParameter(domain);
		DomainType domainType = ControllerHelper.getDomainTypeFromParameter(domain);

		LOGGER.info("Checking status for domain {}", domainType);
		RequestProtocol protocol = protocolMap.get(domainType);

		Status status;
		int percentDone;
		int packagesTodo;

		if(protocol == null) {
			LOGGER.info("No entry found - no request submitted for this domain");
			status = Status.UNKNOWN;
			percentDone = -1;
			packagesTodo = -1;
		}
		else if(protocol.getWorkPackages().isEmpty() && protocol.getWorkPackageResults().isEmpty()) {
			LOGGER.info("Entry found but neither WPs nor results - assuming we did not start yet");
			status = Status.PENDING;
			percentDone = 0;
			packagesTodo = -1;
		}
		else if(!protocol.getWorkPackageResults().isEmpty() && protocol.getWorkPackages().size() == protocol.getWorkPackageResults().size()) {
			LOGGER.info("Entry found, # results equals # WPs - assuming we are done");
			status = Status.DONE;
			percentDone = 100;
			packagesTodo = 0;
		}
		else {
			LOGGER.info("Entry and {} WPs plus {} results found ", protocol.getWorkPackages().size(), protocol.getWorkPackageResults().size());
			status = Status.WORKING;
			percentDone = (int) (((float)protocol.getWorkPackageResults().size() / (float) protocol.getWorkPackages().size()) * 100.0f);
			packagesTodo = protocol.getWorkPackages().size() - protocol.getWorkPackageResults().size();
			LOGGER.info("Status working: {} of {} packages done ({}%) - {} packages to go", protocol.getWorkPackageResults().size(), protocol.getWorkPackages().size(), percentDone, packagesTodo);
		}

		final ComputationStatus result = new ComputationStatus(status, percentDone, packagesTodo);

		ControllerHelper.logRequestFinish(LOGGER, methodName, result, domain);
		return result;
	}

	@GetMapping("/result/{domain}")
	public ResponseEntity<Object> getResult(@PathVariable("domain") String domain) throws URISyntaxException {
		final String methodName = "getResult";
		ControllerHelper.logRequestStart(LOGGER, methodName, domain);

		ControllerHelper.checkParameter(domain);
		DomainType domainType = ControllerHelper.getDomainTypeFromParameter(domain);

		ComputationStatus status = getStatus(domain);
		LOGGER.info("Computation status is {}", status);
		if(Status.DONE != status.getStatus() && Status.FAILED != status.getStatus()) {
			final ComputationResult result = new ComputationResult(status.getStatus(), "Computation not done yet");

			ControllerHelper.logRequestFinish(LOGGER, methodName, result, domain);
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(result);
		}

		RequestProtocol protocol = protocolMap.get(domainType);
		if(protocol == null || protocol.getWorkPackageResults() == null || protocol.getWorkPackageResults().isEmpty()) {
			final ComputationResult result = new ComputationResult(status.getStatus(), "No results found");

			ControllerHelper.logRequestFinish(LOGGER, methodName, result, domain);
			return ResponseEntity.status(HttpStatus.OK).body(result);
		}
		LOGGER.info("Protocol for domain {} contains {} results", domainType, protocol.getWorkPackageResults().size());

		String nodeId = reserveNode();
		if(nodeId == null) {
			final ResponseEntity<Object> response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No free nodes found after max number of retries");
			ControllerHelper.logRequestFinish(LOGGER, methodName, response, domain);
			return response;
		}

		URI nodeUri = nodeRegistry.getUriForNode(nodeId);
		URI uri = new URI(nodeUri + ACCUMULATE_RESULTS_PATH);
		LOGGER.info("Node-URI to be called: {}", uri);

		HttpEntity<ResultsProtocol> entity = (HttpEntity<ResultsProtocol>) ControllerHelper.createHttpEntity(new ResultsProtocol(protocol.getDomain(), protocol.getWorkPackageResults()));

		nodeRegistry.occupyNode(nodeId);
		ResponseEntity<ComputationResult> response = restTemplate.exchange(uri, HttpMethod.POST, entity, ComputationResult.class);

		ComputationResult result = response.getBody();
		LOGGER.info("Computation result received: {}", result);
		if(HttpStatus.OK == response.getStatusCode()) {
			LOGGER.info("Removing protocol from domain {} from result store", domainType);
			protocolMap.remove(domainType);
		}

		nodeRegistry.freeNode(nodeId);

		ControllerHelper.logRequestFinish(LOGGER, methodName, result, domain);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	// TODO copied from ServiceController --> unify
	protected String reserveNode() {
		LOGGER.info("Trying to reserve a node...");
		final int maxRetryCount = controllerProperties.getController().getRetryCount();

		String nodeId = null;
		int i = 0;

		do {
			nodeId = nodeRegistry.reserveNode();
			LOGGER.info("Reserved node is {}", nodeId);

			if(nodeId == null) {
				i++;
				waitForRetry();
			}
		}
		while(nodeId == null && i < maxRetryCount);

		if(nodeId == null) {
			LOGGER.info("No free nodes found after {} retries", maxRetryCount);
		}

		return nodeId;
	}

	private void waitForRetry() {
		try {
			long retryDelay = controllerProperties.getController().getRetryDelay();
			LOGGER.info("Waiting for {} ms before retrying", retryDelay);
			Thread.sleep(retryDelay);
		}
		catch(InterruptedException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

}
