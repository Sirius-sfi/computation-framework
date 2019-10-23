package no.siriuslabs.computationapi.controller;

import no.siriuslabs.computationapi.api.model.computation.DomainType;
import no.siriuslabs.computationapi.api.model.request.ComputationRequest;
import no.siriuslabs.computationapi.api.model.request.Payload;
import no.siriuslabs.computationapi.config.ControllerProperties;
import no.siriuslabs.computationapi.event.ComputationRequestAddedEvent;
import no.siriuslabs.computationapi.service.DataPreparationService;
import no.siriuslabs.computationapi.service.NodeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
public class ServiceController extends AbstractController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceController.class);

	public static final String DATA_VALIDATION_FAILED_RESPONSE_CODE_MSG = "Data validation failed: response code=";
	public static final String DATA_VALIDATION_FAILED_NO_RESULT_MSG = "Data validation failed: no result";
	public static final String DATA_VALIDATION_FAILED_MSG = "Data validation failed: ";
	public static final String VALIDATE_DATA_PATH = "/validateData";

	private final DataPreparationService dataPreparationService;
	private final RestTemplate restTemplate;
	private final ApplicationEventPublisher applicationEventPublisher;

	@Autowired
	public ServiceController(NodeRegistry nodeRegistry, DataPreparationService dataPreparationService, ControllerProperties controllerProperties, RestTemplate restTemplate, ApplicationEventPublisher applicationEventPublisher) {
		super(nodeRegistry, controllerProperties);
		this.dataPreparationService = dataPreparationService;
		this.restTemplate = restTemplate;
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@PostMapping("/startComputation")
	public ResponseEntity<Object> startComputation(@RequestBody ComputationRequest request) throws URISyntaxException, ExecutionException {
		final String methodName = "startComputation";
		logRequestStart(LOGGER, methodName, request);

		DomainType domain = request.getDomain();

		request.setStartedTimestamp(System.currentTimeMillis());

		// TODO check status (task, not node usage) - if busy with that domain --> fail

		String nodeId = reserveNode(domain);
		if(nodeId == null) {
			final ResponseEntity<Object> response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No free nodes found after max number of retries");
			logRequestFinish(LOGGER, methodName, response, request);
			return response;
		}

		Payload payload = request.getPayload();

		ResponseEntity<Object> response = validateData(nodeId, payload);
		if(response == null) {
			LOGGER.info("Validation was successful");

			ComputationRequestAddedEvent event = new ComputationRequestAddedEvent(this, request);
			LOGGER.info("Publishing event: {}", event);
			applicationEventPublisher.publishEvent(event);

			LOGGER.info("Invoking an asynchronous method from {}", Thread.currentThread().getName());
			URI nodeUri = getNodeRegistry().getUriForNode(nodeId);
			dataPreparationService.prepareAndPackageData(nodeId, nodeUri, request);

			response = ResponseEntity.ok("Data accepted, computation submitted");
		}
		else {
			LOGGER.info("Freeing node {} because validation failed", nodeId);
			getNodeRegistry().occupyNode(nodeId);
			getNodeRegistry().freeNode(nodeId);
		}

		logRequestFinish(LOGGER, methodName, response, request);
		return response;
	}

	protected ResponseEntity<Object> validateData(String nodeId, Payload payload) throws URISyntaxException {
		LOGGER.info("Validating data - using node {}; data package = {}", nodeId, payload);

		URI nodeUri = getNodeRegistry().getUriForNode(nodeId);
		URI uri = new URI(nodeUri + VALIDATE_DATA_PATH);
		HttpEntity<Payload> entity = (HttpEntity<Payload>) ControllerHelper.createHttpEntity(payload);

		LOGGER.info("Service to be called @ {} with parameters: {}", uri, payload);

		ResponseEntity<Object> validationResponse = restTemplate.exchange(uri, HttpMethod.POST, entity, Object.class);

		List<String> result = (List<String>) validationResponse.getBody();
		if(HttpStatus.OK != validationResponse.getStatusCode()) {
			LOGGER.info("Validation failed - response code was {}", validationResponse.getStatusCode());
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(DATA_VALIDATION_FAILED_RESPONSE_CODE_MSG + validationResponse.getStatusCode());
		}
		else if(result == null) {
			LOGGER.info("Validation failed - null result");
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(DATA_VALIDATION_FAILED_NO_RESULT_MSG);
		}
		else if(!result.isEmpty()) {
			LOGGER.info("Validation failed - validation errors: {}", result);
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(DATA_VALIDATION_FAILED_MSG + result);
		}

		return null;
	}

}
