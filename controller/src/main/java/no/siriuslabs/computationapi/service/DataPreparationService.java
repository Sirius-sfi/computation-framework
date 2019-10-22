package no.siriuslabs.computationapi.service;

import no.siriuslabs.computationapi.api.model.computation.DomainType;
import no.siriuslabs.computationapi.api.model.computation.WorkPackage;
import no.siriuslabs.computationapi.api.model.request.ComputationRequest;
import no.siriuslabs.computationapi.event.DataPreparartionFinishedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
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
public class DataPreparationService extends AbstractAsynchService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DataPreparationService.class);

	protected static final String SERVICE_PATH = "/prepareAndPackageData";

	@Autowired
	public DataPreparationService(NodeRegistry nodeRegistry, ApplicationEventPublisher applicationEventPublisher) {
		super(nodeRegistry, applicationEventPublisher);
	}

	@Async
	public void prepareAndPackageData(String nodeId, URI nodeUri, ComputationRequest request) throws URISyntaxException, ExecutionException {
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

	private List<WorkPackage> getWorkPackagesFromResponse(ResponseEntity<Object> response) {
		List<Map<String, Object>> result = (List<Map<String, Object>>) response.getBody();
		List<WorkPackage> workPackages = new ArrayList<>(result.size());

		for(Map<String, Object> row : result) {
			final DomainType domain = DomainType.valueOf((String) row.get("domain"));
			final Number id = (Number) row.get("id");
			WorkPackage workPackage = new WorkPackage(domain, id.longValue());
			workPackage.setRunId((Integer) row.get("runId"));
			workPackage.setData((Map<String, Object>) row.get("data"));
			workPackages.add(workPackage);
		}
		return workPackages;
	}

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
