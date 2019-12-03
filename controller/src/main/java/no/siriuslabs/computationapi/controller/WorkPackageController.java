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
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Rest controller responsible for keeping track of the work packages still to run and for distributing the work to different nodes.
 */
// TODO technically not a Rest controller anymore since it lost domain information --> rename/remove annotation/move?
@RestController
public class WorkPackageController extends AbstractController implements ApplicationListener<AbstractDataWorkflowEvent> {

	private static final Logger LOGGER = LoggerFactory.getLogger(WorkPackageController.class);

	/**
	 * ComputationJobService that runs the asynchronous computations.
	 */
	private final ComputationJobService computationJobService;
	/**
	 * Reference to ResultController to get information about results and overall status.
	 */
	private final ResultController resultController;

	/**
	 * Map that keeps all WorkPackages ordered by DomainType.
	 */
	private final ConcurrentHashMap<DomainType, ConcurrentLinkedQueue<WorkPackage>> workToDo;
	/**
	 * Map that keeps a register of which WorkPackage currently runs on which node.
	 */
	private final ConcurrentHashMap<Long, Pair<WorkPackage, String>> runningWorkPackages;

	/**
	 * Autowired constructor.
	 */
	@Autowired
	public WorkPackageController(NodeRegistry nodeRegistry, ComputationJobService computationJobService, ControllerProperties controllerProperties, ResultController resultController) {
		super(nodeRegistry, controllerProperties);
		this.computationJobService = computationJobService;
		this.resultController = resultController;
		workToDo = new ConcurrentHashMap<>(5);
		runningWorkPackages = new ConcurrentHashMap<>();
	}

	/**
	 * Implementation of ApplicationListener to keep track of different application events reporting the completion of the preparation phase or a computation finishing for a WorkPackage.
	 * Different event classes are used here, depending on the application phase the event belongs to.
	 */
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

	/**
	 * Triggers distribution of work packages to all idle nodes that have a matching DomainType. Called from a timer regularly.
	 */
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

	/**
	 * Method that tries to find WorkPackages that were given to a node to be computed but have never reported a result.<p>
	 * If such a WorkPackage is identified, it will be re-added to the queue.
	 */
	private void handleLostPackages() {
		final DomainType domain = getNodeRegistry().getDomain();
		RequestProtocol protocol = resultController.getProtocolForDomain(domain);
		if(protocol == null) { // protocol might be null even if queue a exists and sent us here if results of previous run have already been collected
			LOGGER.info("No protocol present --> no lost packages possible");
			return;
		}

		final int numberOfPackages = protocol.getWorkPackages().size();
		final int numberOfResults = protocol.getWorkPackageResults().size();

		if(resultController.getStatus(domain.getDomainType()).getPackagesToDo() > 0 && numberOfPackages != numberOfResults) {
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

	/**
	 * Filters WorkPackages from the lost-package-candidates in the given list of packagesToDo that are found not to be lost, because they already have a result in
	 * the RequestProtocol or are currently running on a node that is known to be alive.
	 */
	private void filterOutValidEntries(RequestProtocol protocol, List<WorkPackage> packagesToDo) {
		List<WorkPackage> packagesWithResult = protocol.getWorkPackageResults().stream().map(WorkPackageResult::getWorkPackage).collect(Collectors.toCollection(() -> new ArrayList<>(protocol.getWorkPackageResults().size())));

		for(WorkPackage w : packagesWithResult) {
			packagesToDo.remove(w);
		}
		for(Pair<WorkPackage, String> p : runningWorkPackages.values()) {
			if(getNodeRegistry().hasNode(p.getY())) { // TODO also include ping status here later
				packagesToDo.remove(p.getX());
			}
		}
	}

	/**
	 * Distributes WorkPackages to worker nodes with a matching DomainType as long as there are some in the given queue and as long as nodes can be reserved for that task.
	 * Each WorkPackage is assigned to a node and run by an asynchronous service then.
	 */
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
