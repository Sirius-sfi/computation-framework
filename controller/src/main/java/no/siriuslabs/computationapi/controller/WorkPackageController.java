package no.siriuslabs.computationapi.controller;

import no.siriuslabs.computationapi.api.model.computation.DomainType;
import no.siriuslabs.computationapi.api.model.computation.RequestProtocol;
import no.siriuslabs.computationapi.api.model.computation.WorkPackage;
import no.siriuslabs.computationapi.api.model.computation.WorkPackageResult;
import no.siriuslabs.computationapi.config.ControllerProperties;
import no.siriuslabs.computationapi.event.AbstractDataWorkflowEvent;
import no.siriuslabs.computationapi.event.DataPreparartionFinishedEvent;
import no.siriuslabs.computationapi.event.ResultUpdateEvent;
import no.siriuslabs.computationapi.service.ComputationJobService;
import no.siriuslabs.computationapi.service.NodeRegistry;
import no.siriuslabs.computationapi.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@RestController
public class WorkPackageController extends AbstractController implements ApplicationListener<AbstractDataWorkflowEvent> {

	private static final Logger LOGGER = LoggerFactory.getLogger(WorkPackageController.class);

	private final ComputationJobService computationJobService;
	private final ResultController resultController;

	private final ConcurrentHashMap<DomainType, ConcurrentLinkedQueue<WorkPackage>> workToDo;
	private final ConcurrentHashMap<Long, Pair<WorkPackage, String>> runningWorkPackages;

	@Autowired
	public WorkPackageController(NodeRegistry nodeRegistry, ComputationJobService computationJobService, ControllerProperties controllerProperties, ResultController resultController) {
		super(nodeRegistry, controllerProperties);
		this.computationJobService = computationJobService;
		this.resultController = resultController;
		workToDo = new ConcurrentHashMap<>(DomainType.values().length);
		runningWorkPackages = new ConcurrentHashMap<>();
	}

	@Override
	public void onApplicationEvent(AbstractDataWorkflowEvent event) {
		if(event instanceof DataPreparartionFinishedEvent) {
			DataPreparartionFinishedEvent dataPreparartionFinishedEvent = (DataPreparartionFinishedEvent) event;
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
		else if(event instanceof ResultUpdateEvent) {
			ResultUpdateEvent resultUpdateEvent = (ResultUpdateEvent) event;
			LOGGER.info("ResultUpdateEvent triggered in thread {} with data {}", Thread.currentThread().getName(), resultUpdateEvent);

			runningWorkPackages.remove(resultUpdateEvent.getWorkPackageResult().getWorkPackage().getId());
		}
		else {
			LOGGER.info("Unknown triggered in thread {} with data {}", Thread.currentThread().getName(), event);
		}
	}

	public void distributeWork() {
		final String methodName = "distributeWork";
		logRequestStart(LOGGER, methodName);
		try {
			final DomainType domain = getNodeRegistry().getDomain();
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

				handleLostPackages();
			}
			else {
				LOGGER.info("Queue for domain {} has {} packages", domain, queue.size());

				distributeWorkToNodes(queue);
			}
		}
		catch(URISyntaxException e) {
			LOGGER.error(e.getMessage(), e);
		}
		finally {
			logVoidRequestFinish(LOGGER, methodName);
		}
	}

	private void handleLostPackages() {
		final DomainType domain = getNodeRegistry().getDomain();
		RequestProtocol protocol = resultController.getProtocolForDomain(domain);
		final int numberOfPackages = protocol.getWorkPackages().size();
		final int numberOfResults = protocol.getWorkPackageResults().size();

		if(resultController.getStatus(domain.name()).getPackagesToDo() > 0 && numberOfPackages != numberOfResults) {
			LOGGER.info("Possible lost packages found --> # WPs={} / # Results={} / # active={}", numberOfPackages, numberOfResults, runningWorkPackages.size());
			List<WorkPackage> packagesToDo = new ArrayList<>(protocol.getWorkPackages());

			filterOutValidEntries(protocol, packagesToDo);

			if(packagesToDo.isEmpty()) {
				LOGGER.info("No lost packages found");
			}
			else {
				LOGGER.info("{} lost packages found:", packagesToDo.size());
				for(WorkPackage w : packagesToDo) {
					LOGGER.info("\t\t Adding lost package # {} back to queue", w.getId());
					// TODO do we need to remove these WPs from the runningWPs map??

					workToDo.get(domain).add(w);
				}
			}
		}
	}

	private void filterOutValidEntries(RequestProtocol protocol, List<WorkPackage> packagesToDo) {
		List<WorkPackage> packagesWithResult = protocol.getWorkPackageResults().stream().map(WorkPackageResult::getWorkPackage).collect(Collectors.toCollection(() -> new ArrayList<>(protocol.getWorkPackageResults().size())));

		for(WorkPackage w : packagesWithResult) {
			packagesToDo.remove(w);
		}
		for(Pair<WorkPackage, String> p : runningWorkPackages.values()) {
			if(getNodeRegistry().hasNode(p.getY())) {
				packagesToDo.remove(p.getX());
			}
		}
	}

	private void distributeWorkToNodes(ConcurrentLinkedQueue<WorkPackage> queue) throws URISyntaxException {
		while(true) {
			String nodeId = getNodeRegistry().reserveNode(getNodeRegistry().getDomain());
			LOGGER.info("Reserved node {} to do some work", nodeId);

			if(nodeId == null) {
				LOGGER.info("No free node available --> cancelling work");
				break;
			}

			WorkPackage workPackage = queue.poll();
			if(workPackage == null) {
				LOGGER.info("Queue seems to be empty (unexpectedly) --> cancelling work and freeing up node");
				getNodeRegistry().freeNode(nodeId);
				break;
			}

			LOGGER.info("WorkPackage ready and node reserved - we can do something");

			LOGGER.info("Invoking an asynchronous method from {}", Thread.currentThread().getName());
			URI nodeUri = getNodeRegistry().getUriForNode(nodeId);

			runningWorkPackages.put(workPackage.getId(), new Pair<>(workPackage, nodeId));

			computationJobService.runComputation(nodeId, nodeUri, workPackage);
		}
	}

}
