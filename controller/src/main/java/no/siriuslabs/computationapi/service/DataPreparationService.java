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
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Spring service that is used to asynchronously run the data preparation and work package generation step of the pipeline.
 * It uses functionality of AbstractAsynchService and provides only one public method to start the generation of work packages on a worker node.
 */
@Service
public class DataPreparationService extends AbstractAsynchService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DataPreparationService.class);

	/**
	 * Relative path of the worker node service to be called.
	 */
	protected static final String SERVICE_PATH = "/prepareAndPackageData";

	/**
	 * Autowired constructor.
	 */
	@Autowired
	public DataPreparationService(NodeRegistry nodeRegistry, ApplicationEventPublisher applicationEventPublisher) {
		super(nodeRegistry, applicationEventPublisher);
	}

	/**
	 * Starts the asynchronous generation of work packages from the given ComputationRequest on a worker node. The result is reported using events.
	 * @param nodeId	Identifier of the node that is to be called.
	 * @param nodeUri	URI of the node to be called.
	 * @param request	ComputationRequest containing the incoming data to gererate the work packages from.
	 * @throws URISyntaxException	if the parameter nodeUri and the result of getServicePath() should not combine to a valid URI.
	 */
	@Async
	public void prepareAndPackageData(String nodeId, URI nodeUri, ComputationRequest request) throws URISyntaxException {
		LOGGER.info("Executing asynchronously in thread {}", Thread.currentThread().getName());

		long startTime = System.currentTimeMillis();

		ResponseEntity<Object> response = callNodeWebservice(nodeId, nodeUri, request);

		List<WorkPackage> workPackages = getWorkPackagesFromResponse(response);
		addStatsToRequest(nodeId, request, startTime, workPackages);

		DataPreparartionFinishedEvent event = new DataPreparartionFinishedEvent(this, request, workPackages);
		LOGGER.info("Publishing event: {}", event);
		getApplicationEventPublisher().publishEvent(event);

		LOGGER.info("Asynchronous execution finished");
	}

	/**
	 * Extracts the generated WorkPackages from the given ResponseEntity and returns them as a List.
	 */
	private List<WorkPackage> getWorkPackagesFromResponse(ResponseEntity<Object> response) {
		List<Map<String, Object>> result = (List<Map<String, Object>>) response.getBody();
		List<WorkPackage> workPackages = new ArrayList<>(result.size());

		for(Map<String, Object> row : result) {
			final DomainType domain = ControllerHelper.getDomainTypeFromParameter((String) row.get("domain"));
			final Number id = (Number) row.get("id");
			WorkPackage workPackage = new WorkPackage(domain, id.longValue());
			workPackage.setRunId((Integer) row.get("runId"));
			workPackage.setData((Map<String, Object>) row.get("data"));
			workPackages.add(workPackage);
		}
		return workPackages;
	}

	/**
	 * Adds some statistical data to the result.
	 * @param nodeId		Identifier of the node the computation run on.
	 * @param request		ComputationRequest the data should be added to.
	 * @param startTime		Timestamp the computation started.
	 * @param workPackages	List of generated WorkPackages.
	 */
	private void addStatsToRequest(String nodeId, ComputationRequest request, long startTime, List<WorkPackage> workPackages) {
		long finishTime = System.currentTimeMillis();
		request.setPreparationTime(finishTime - startTime);
		request.setNumberNodesStart(getNodeRegistry().getNumberOfNodes());
		request.setNumberWPs(workPackages.size());
		LOGGER.info("Preparation phase on node {} took {} ms", nodeId, finishTime - startTime);
	}

	@Override
	protected String getServicePath() {
		return SERVICE_PATH;
	}
}
