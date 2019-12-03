package no.siriuslabs.computationapi.controller;

import no.siriuslabs.computationapi.api.model.computation.ComputationResult;
import no.siriuslabs.computationapi.api.model.computation.ComputationStatus;
import no.siriuslabs.computationapi.api.model.computation.DomainType;
import no.siriuslabs.computationapi.api.model.computation.RequestProtocol;
import no.siriuslabs.computationapi.api.model.computation.ResultsProtocol;
import no.siriuslabs.computationapi.api.model.computation.Status;
import no.siriuslabs.computationapi.api.model.computation.WorkPackage;
import no.siriuslabs.computationapi.api.model.computation.WorkPackageResult;
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

/**
 * Rest controller responsible for keeping track of computation status and results and accumulating them in the end.<p>
 * This controller is listener to several types of events publishing the progress of a computation run and/or the conclusion of steps in the process.
 */
@RestController
public class ResultController extends AbstractController implements ApplicationListener<AbstractDataWorkflowEvent> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResultController.class);

	/**
	 * Part of a worker node URL used to call that node's accumulateResults-service.
	 */
	private static final String ACCUMULATE_RESULTS_PATH = "/accumulateResults";

	/**
	 * Spring RestTemplate used to call worker node services.
	 */
	private final RestTemplate restTemplate;

	/**
	 * Protocol of everything that happened so far in a computation run. Can keep track of several runs, as long as their DomainType is different.
	 */
	private Map<DomainType, RequestProtocol> protocolMap = new ConcurrentHashMap<>();

	/**
	 * Autowired constructor.
	 */
	@Autowired
	public ResultController(NodeRegistry nodeRegistry, ControllerProperties controllerProperties/*, RestTemplate restTemplate*/) {
		super(nodeRegistry, controllerProperties);
//		this.restTemplate = restTemplate; // TODO RestTemplate causes cyclic dependency in Spring
		this.restTemplate = new RestTemplate();
	}

	/**
	 * Implementation of ApplicationListener to keep track of different application events reporting the progress of a computation run and/or the conclusion of single steps in the process.<p>
	 * Different event classes are used here, depending on the application phase the event belongs to.
	 */
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

	/**
	 * Returns a ComputationStatus object depicting the current status of the computation of the given domain.<p>
	 * The returned object contains three-part information:
	 * <ul>
	 *     <li>The computation status - UNKNOWN (nothing found for this domain), PENDING (found but no WorkPackages present yet), WORKING (WorkPackages found) and DONE (finished, results not collected)</li>
	 *     <li>Percentage of WorkPackages done</li>
	 *     <li>Number of WorkPackages still to do (without results)</li>
	 * </ul>
	 */
	@GetMapping("/status/{domain}")
	public ComputationStatus getStatus(@PathVariable("domain") String domain) {
		final String methodName = "getStatus";
		logRequestStart(LOGGER, methodName, domain);

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

		logRequestFinish(LOGGER, methodName, result, domain);
		return result;
	}

	/**
	 * Triggers the domain specific accumulation of the collected results on a worker node.<p>
	 * Returns ResponseEntity containing general statistical data about the computation run and a domain specific result if successful or an error or a negative
	 * reply if there are no results (yet) or something went wrong.<p>
	 * After results have been found and reported back successfully they will be removed the controller and this DomainType is applicable to be used in a further computation run again.
	 */
	@GetMapping("/result/{domain}")
	public ResponseEntity<Object> getResult(@PathVariable("domain") String domain) throws URISyntaxException {
		final String methodName = "getResult";
		logRequestStart(LOGGER, methodName, domain);

		ControllerHelper.checkParameter(domain);
		DomainType domainType = ControllerHelper.getDomainTypeFromParameter(domain);

		ComputationStatus status = getStatus(domain);
		LOGGER.info("Computation status is {}", status);
		if(Status.DONE != status.getStatus() && Status.FAILED != status.getStatus()) {
			final ComputationResult result = new ComputationResult(status.getStatus(), "Computation not done yet");

			logRequestFinish(LOGGER, methodName, result, domain);
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(result);
		}

		RequestProtocol protocol = protocolMap.get(domainType);
		if(protocol == null || protocol.getWorkPackageResults() == null || protocol.getWorkPackageResults().isEmpty()) {
			final ComputationResult result = new ComputationResult(status.getStatus(), "No results found");

			logRequestFinish(LOGGER, methodName, result, domain);
			return ResponseEntity.status(HttpStatus.OK).body(result);
		}
		LOGGER.info("Protocol for domain {} contains {} results", domainType, protocol.getWorkPackageResults().size());

		String nodeId = reserveNode(domainType);
		if(nodeId == null) {
			final ResponseEntity<Object> response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No free nodes found after max number of retries");
			logRequestFinish(LOGGER, methodName, response, domain);
			return response;
		}

		URI nodeUri = getNodeRegistry().getUriForNode(nodeId);
		URI uri = new URI(nodeUri + ACCUMULATE_RESULTS_PATH);
		LOGGER.info("Node-URI to be called: {}", uri);

		final ResultsProtocol resultsProtocol = new ResultsProtocol(protocol.getDomain(), protocol.getWorkPackageResults());
		addTimingData(protocol, resultsProtocol);

		HttpEntity<ResultsProtocol> entity = (HttpEntity<ResultsProtocol>) ControllerHelper.createHttpEntity(resultsProtocol);

		getNodeRegistry().occupyNode(nodeId);
		ResponseEntity<ComputationResult> response = restTemplate.exchange(uri, HttpMethod.POST, entity, ComputationResult.class);

		ComputationResult result = response.getBody();
		LOGGER.info("Computation result received: {}", result);
		if(HttpStatus.OK == response.getStatusCode()) {
			LOGGER.info("Removing protocol from domain {} from result store", domainType);
			protocolMap.remove(domainType);
		}

		getNodeRegistry().freeNode(nodeId);

		logRequestFinish(LOGGER, methodName, result, domain);
		return ResponseEntity.status(HttpStatus.OK).body(result);
	}

	/**
	 * Prepares and adds all available statistical and timing data in the given RequestProtocol to the given ResultsProtocol.
	 */
	private void addTimingData(RequestProtocol protocol, ResultsProtocol resultsProtocol) {
		resultsProtocol.setStartedTimestamp(protocol.getComputationRequest().getStartedTimestamp());
		resultsProtocol.setPreparationTime(protocol.getComputationRequest().getPreparationTime());
		resultsProtocol.setNumberWPs(protocol.getWorkPackages().size());
		resultsProtocol.setNumberNodesStart(protocol.getComputationRequest().getNumberNodesStart());
		resultsProtocol.setNumberNodesEnd(getNodeRegistry().getNumberOfNodes());

		long latestFinishTimestamp = 0;
		long minWpTime = Long.MAX_VALUE;
		long maxWpTime = 0;
		long wpSum = 0;
		for(WorkPackageResult res : protocol.getWorkPackageResults()) {
			if(latestFinishTimestamp < res.getFinishedTimestamp()) {
				latestFinishTimestamp = res.getFinishedTimestamp();
			}
			if(minWpTime > res.getRunningTime()) {
				minWpTime = res.getRunningTime();
			}
			if(maxWpTime < res.getRunningTime()) {
				maxWpTime = res.getRunningTime();
			}

			wpSum += res.getRunningTime();
		}

		resultsProtocol.setFinishedTimestamp(latestFinishTimestamp);
		resultsProtocol.setMinWpTime(minWpTime);
		resultsProtocol.setMaxWpTime(maxWpTime);
		resultsProtocol.setAvgWpTime(wpSum / protocol.getWorkPackageResults().size());
	}

	protected RequestProtocol getProtocolForDomain(DomainType domainType) {
		return protocolMap.get(domainType);
	}

}
