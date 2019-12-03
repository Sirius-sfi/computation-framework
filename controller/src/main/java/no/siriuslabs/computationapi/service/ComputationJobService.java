package no.siriuslabs.computationapi.service;

import no.siriuslabs.computationapi.api.model.computation.WorkPackage;
import no.siriuslabs.computationapi.api.model.computation.WorkPackageResult;
import no.siriuslabs.computationapi.controller.ControllerHelper;
import no.siriuslabs.computationapi.event.ResultUpdateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Spring service that is used to asynchronously run computations of work packages.<p>
 * It uses functionality of AbstractAsynchService and provides only one public method to start a work package's computation on a worker node.
 */
@Service
public class ComputationJobService extends AbstractAsynchService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ComputationJobService.class);

	/**
	 * Relative path of the worker node service to be called.
	 */
	protected static final String SERVICE_PATH = "/runComputation";

	/**
	 * Autowired constructor.
	 */
	@Autowired
	public ComputationJobService(NodeRegistry nodeRegistry, ApplicationEventPublisher applicationEventPublisher) {
		super(nodeRegistry, applicationEventPublisher);
	}

	@Override
	protected String getServicePath() {
		return SERVICE_PATH;
	}

	/**
	 * Starts the asynchronous computation of the given WorkPackage on a worker node. The result is reported using events.
	 * @param nodeId 		Identifier of the node that is to be called.
	 * @param nodeUri 		URI of the node to be called.
	 * @param workPackage 	WorkPackage that is to be computed.
	 * @throws URISyntaxException if the parameter nodeUri and the result of getServicePath() should not combine to a valid URI.
	 */
	@Async
	public void runComputation(String nodeId, URI nodeUri, WorkPackage workPackage) throws URISyntaxException {
		LOGGER.info("Executing asynchronously in thread {}", Thread.currentThread().getName());

		long startTime = System.currentTimeMillis();

		ResponseEntity<Object> response = callNodeWebservice(nodeId, nodeUri, workPackage);

		WorkPackageResult result = getResultFromResponse(response);
		addStatsToResult(nodeId, startTime, result);

		ResultUpdateEvent event = new ResultUpdateEvent(this, result);
		LOGGER.info("Publishing event: {}", event);
		getApplicationEventPublisher().publishEvent(event);

		LOGGER.info("Asynchronous execution finished");
	}

	/**
	 * Extracts the result from the given ResponseEntity and returns it as a WorkPackageResult.
	 */
	private WorkPackageResult getResultFromResponse(ResponseEntity<Object> response) {
		Map<String, Object> resultMap = (Map<String, Object>) response.getBody();

		Map<String, Object> wpMap = (Map<String, Object>) resultMap.get("workPackage");
		WorkPackage wp = new WorkPackage(ControllerHelper.getDomainTypeFromParameter((String) wpMap.get("domain")), ((Number) wpMap.get("id")).longValue());
		wp.setRunId((Integer) wpMap.get("runId"));
		wp.setData((Map<String, Object>) wpMap.get("data"));

		final WorkPackageResult result = new WorkPackageResult(wp);
		Map<String, Object> data = (Map<String, Object>) resultMap.get("data");
		result.setData(data);

		return result;
	}

	/**
	 * Adds some statistical data to the result.
	 * @param nodeId	Identifier of the node the computation run on.
	 * @param startTime	Timestamp the computation started.
	 * @param result	WorkPackageResult the data should be added to.
	 */
	private void addStatsToResult(String nodeId, long startTime, WorkPackageResult result) {
		result.setNodeId(nodeId);
		final long finishTime = System.currentTimeMillis();
		result.setFinishedTimestamp(finishTime);
		result.setRunningTime(finishTime - startTime);
		LOGGER.info("Computation on node {} took {} ms", nodeId, finishTime - startTime);
	}

}
