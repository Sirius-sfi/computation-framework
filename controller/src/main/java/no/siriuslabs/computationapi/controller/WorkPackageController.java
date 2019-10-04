package no.siriuslabs.computationapi.controller;

import no.siriuslabs.computationapi.api.model.computation.DomainType;
import no.siriuslabs.computationapi.api.model.computation.WorkPackage;
import no.siriuslabs.computationapi.event.DataPreparartionFinishedEvent;
import no.siriuslabs.computationapi.service.ComputationJobService;
import no.siriuslabs.computationapi.service.NodeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@RestController
public class WorkPackageController implements ApplicationListener<DataPreparartionFinishedEvent> {

	private static final Logger LOGGER = LoggerFactory.getLogger(WorkPackageController.class);

	private final NodeRegistry nodeRegistry;
	private final ComputationJobService computationJobService;

	private final ConcurrentHashMap<DomainType, ConcurrentLinkedQueue<WorkPackage>> workToDo;

	// TODO rework for multi-domains later
	// TODO offer service to set/add/remove domains?
	private DomainType domain;

	@Autowired
	public WorkPackageController(NodeRegistry nodeRegistry, ComputationJobService computationJobService) {
		this.nodeRegistry = nodeRegistry;
		this.computationJobService = computationJobService;
		workToDo = new ConcurrentHashMap<>(DomainType.values().length);
	}

	@Override
	public void onApplicationEvent(DataPreparartionFinishedEvent dataPreparartionFinishedEvent) {
		LOGGER.info("DataPreparartionFinishedEvent triggered in thread {} with data {}", Thread.currentThread().getName(), dataPreparartionFinishedEvent);

		final DomainType domain = dataPreparartionFinishedEvent.getDomain();
		if(workToDo.get(domain) == null) {
			LOGGER.info("Creating queue for domain {}", domain);
			workToDo.put(domain, new ConcurrentLinkedQueue<>());
		}

		final List<WorkPackage> data = dataPreparartionFinishedEvent.getWorkPackages();
		LOGGER.info("Adding {} work packages to current queue size of {}", data.size(), workToDo.get(domain).size());
		workToDo.get(domain).addAll(data);
	}

	public void distributeWork() {
		final String methodName = "distributeWork";
		ControllerHelper.logRequestStart(LOGGER, methodName, domain);
		try {
			if(domain == null) {
				LOGGER.warn("Domain is not set");
				return;
			}

			ConcurrentLinkedQueue<WorkPackage> queue = workToDo.get(domain);
			if(queue == null) {
				LOGGER.info("Queue for domain {} does not exist - nothing to do", domain);
			}
			else if(queue.isEmpty()) {
				LOGGER.info("Queue for domain {} is empty - nothing to do", domain);
			}
			else {
				LOGGER.info("Queue for domain {} has {} packages", domain, queue.size());

				while(true) {
					String nodeId = nodeRegistry.reserveNode();
					LOGGER.info("Reserved node {} to do some work", nodeId);

					if(nodeId == null) {
						LOGGER.info("No free node available --> cancelling work");
						break;
					}

					WorkPackage workPackage = queue.poll();
					if(workPackage == null) {
						LOGGER.info("Queue seems to be empty (unexpectedly) --> cancelling work and freeing up node");
						nodeRegistry.freeNode(nodeId);
						break;
					}

					LOGGER.info("WorkPackage ready and node reserved - we can do something");

					LOGGER.info("Invoking an asynchronous method from {}", Thread.currentThread().getName());
					URI nodeUri = nodeRegistry.getUriForNode(nodeId);

					computationJobService.runComputation(nodeId, nodeUri, workPackage);
				}
			}
		}
		catch(URISyntaxException e) {
			LOGGER.error(e.getMessage(), e);
		}
		finally {
			ControllerHelper.logVoidRequestFinish(LOGGER, methodName);
		}
	}

	@GetMapping("activeDomain")
	public String getActiveDomain() {
		final String methodName = "getNodeList";
		ControllerHelper.logRequestStart(LOGGER, methodName);

		String result = domain == null ? null : domain.name();

		ControllerHelper.logRequestFinish(LOGGER, methodName, result);
		return result;
	}

	public void setDomain(DomainType domain) {
		this.domain = domain;
	}
}
