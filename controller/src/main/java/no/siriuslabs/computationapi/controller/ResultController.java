package no.siriuslabs.computationapi.controller;

import no.siriuslabs.computationapi.api.model.computation.ComputationResult;
import no.siriuslabs.computationapi.api.model.computation.ComputationStatus;
import no.siriuslabs.computationapi.api.model.computation.DomainType;
import no.siriuslabs.computationapi.api.model.computation.Status;
import no.siriuslabs.computationapi.api.model.computation.WorkPackage;
import no.siriuslabs.computationapi.event.ComputationRequestAddedEvent;
import no.siriuslabs.computationapi.event.DataPreparartionFinishedEvent;
import no.siriuslabs.computationapi.event.AbstractDataWorkflowEvent;
import no.siriuslabs.computationapi.event.ResultUpdateEvent;
import no.siriuslabs.computationapi.model.RequestProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class ResultController implements ApplicationListener<AbstractDataWorkflowEvent> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResultController.class);

	private Map<DomainType, RequestProtocol> protocolMap = new ConcurrentHashMap<>();

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
	public ComputationResult getResult(@PathVariable("domain") String domain) {
		final String methodName = "getResult";
		ControllerHelper.logRequestStart(LOGGER, methodName, domain);

		ControllerHelper.checkParameter(domain);
		DomainType domainType = ControllerHelper.getDomainTypeFromParameter(domain);

		// TODO if computation finished get result data and transform
		// TODO call node with result accumulation request to make one result from WP results
		// TODO return real data and remove protocol
		final ComputationResult result = new ComputationResult(Status.FAILED, "NYI");

		ControllerHelper.logRequestFinish(LOGGER, methodName, result, domain);
		return result;
	}
}
