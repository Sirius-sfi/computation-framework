package no.siriuslabs.computationapi.controller;

import no.siriuslabs.computationapi.api.model.computation.DomainType;
import no.siriuslabs.computationapi.config.ControllerProperties;
import no.siriuslabs.computationapi.service.NodeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Abstract superclass for controller side Rest controllers.<p>
 * Provides some functionality shared by several controllers such as reserving worker nodes, waiting for a while and convenience logging methods.
 */
public abstract class AbstractController {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractController.class);

	/**
	 * Constant used for logging output.
	 */
	private static final String[] EMPTY_PARAMETERS = {};

	/**
	 * Registry that keeps track of worker nodes and their state.
	 */
	private final NodeRegistry nodeRegistry;
	/**
	 * Spring configuration with contents of config file.
	 */
	private final ControllerProperties controllerProperties;

	/**
	 * Constructor accepting the NodeRegistry and the configuration object (to be injected into the concrete implementation class).
	 */
	protected AbstractController(NodeRegistry nodeRegistry, ControllerProperties controllerProperties) {
		this.nodeRegistry = nodeRegistry;
		this.controllerProperties = controllerProperties;
	}

	protected ControllerProperties getControllerProperties() {
		return controllerProperties;
	}

	protected NodeRegistry getNodeRegistry() {
		return nodeRegistry;
	}

	/**
	 * Tries to reserve a WorkerNode for use with the given domain type.<p>
	 * Reserving a node blocks it for other domains and tasks. It can be given something to do after a successful reservation or returned to the pool without being used.<p>
	 * The method asks for a free node that is capable to run tasks for the given domain type. If a node is available, the method returns the assigned node's ID.
	 * If no compatible node is available, the method retries for a configured number of times. Should still no node be found after the maximum number of retires, null will be returned instead of a node ID.
	 */
	protected String reserveNode(DomainType domainType) {
		LOGGER.info("Trying to reserve a node...");
		final int maxRetryCount = controllerProperties.getController().getRetryCount();

		String nodeId = null;
		int i = 0;

		do {
			nodeId = nodeRegistry.reserveNode(domainType);
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

	/**
	 * Waits for an amount of time specified in the config file and then returns.
	 */
	protected void waitForRetry() {
		try {
			long retryDelay = controllerProperties.getController().getRetryDelay();
			LOGGER.info("Waiting for {} ms before retrying", retryDelay);
			Thread.sleep(retryDelay);
		}
		catch(InterruptedException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

	/**
	 * Logs the beginning of a service request. To be used to mark the very start of an exposed controller method.
	 * @param logger		Logger to be used for the output.
	 * @param methodName    Method name of the starting request.
	 */
	public void logRequestStart(Logger logger, String methodName) {
		logRequestStart(logger, methodName, (Object[]) null);
	}

	/**
	 * Logs the beginning of a service request. To be used to mark the very start of an exposed controller method.
	 * @param logger		Logger to be used for the output.
	 * @param methodName    Method name of the starting request.
	 * @param parameters 	List of parameters that were passed to the method being logged.
	 */
	public void logRequestStart(Logger logger, String methodName, Object... parameters) {
		logger.info("Starting service request: {}() with parameters: {}",
				methodName, (parameters == null) ? EMPTY_PARAMETERS : Arrays.toString(parameters));
	}

	/**
	 * Logs the end of a service request that does not return a result. To be used to mark the very end of an exposed controller method with a void return type.
	 * @param logger		Logger to be used for the output.
	 * @param methodName	Method name of the finishing request.
	 */
	public void logVoidRequestFinish(Logger logger, String methodName) {
		logVoidRequestFinish(logger, methodName, (Object[]) null);
	}

	/**
	 * Logs the end of a service request that does not return a result. To be used to mark the very end of an exposed controller method with a void return type.
	 * @param logger		Logger to be used for the output.
	 * @param methodName	Method name of the finishing request.
	 * @param parameters 	List of parameters that were passed to the method being logged.
	 */
	public void logVoidRequestFinish(Logger logger, String methodName, Object... parameters) {
		logRequestFinish(logger, methodName, null, true, parameters);
	}

	/**
	 * Logs the end of a service request that does return a result. To be used to mark the very end of an exposed controller method with a non-void return type.
	 * @param logger		Logger to be used for the output.
	 * @param methodName	Method name of the finishing request.
	 * @param result 		Value that will be returned by the method being logged.
	 */
	public void logRequestFinish(Logger logger, String methodName, Object result) {
		logRequestFinish(logger, methodName, result, (Object[]) null);
	}

	/**
	 * Logs the end of a service request that does return a result. To be used to mark the very end of an exposed controller method with a non-void return type.
	 * @param logger		Logger to be used for the output.
	 * @param methodName	Method name of the finishing request.
	 * @param parameters 	List of parameters that were passed to the method being logged.
	 * @param result 		Value that will be returned by the method being logged.
	 */
	public void logRequestFinish(Logger logger, String methodName, Object result, Object... parameters) {
		logRequestFinish(logger, methodName, result, false, parameters);
	}

	/**
	 * Internal method that logs the finishing of request in various combinations.
	 * @param logger		Logger to be used for the output.
	 * @param methodName	Method name of the finishing request.
	 * @param result		Value that will be returned by the method being logged. Use null if return type is void.
	 * @param wasCallVoid	Flag that shows if the method's return type is void (true) or not (false).
	 * @param parameters	List of parameters that were passed to the method being logged. Use null if no parameters were passed.
	 */
	private void logRequestFinish(Logger logger, String methodName, Object result, boolean wasCallVoid, Object... parameters) {
		logger.info("Finished service request: {}() with parameters: {} {}",
				methodName, (parameters == null) ? EMPTY_PARAMETERS : Arrays.toString(parameters), (wasCallVoid ? " for void result" : " for result: " + result));
	}
}
