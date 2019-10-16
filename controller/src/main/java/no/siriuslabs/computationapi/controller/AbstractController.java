package no.siriuslabs.computationapi.controller;

import no.siriuslabs.computationapi.config.ControllerProperties;
import no.siriuslabs.computationapi.service.NodeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public abstract class AbstractController {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractController.class);

	private static final String[] EMPTY_PARAMETERS = {};

	private final NodeRegistry nodeRegistry;
	private final ControllerProperties controllerProperties;

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

	protected String reserveNode() {
		LOGGER.info("Trying to reserve a node...");
		final int maxRetryCount = controllerProperties.getController().getRetryCount();

		String nodeId = null;
		int i = 0;

		do {
			nodeId = nodeRegistry.reserveNode();
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

	public void logRequestStart(Logger logger, String methodName) {
		logRequestStart(logger, methodName, (Object[]) null);
	}

	public void logRequestStart(Logger logger, String methodName, Object... parameters) {
		logger.info("Starting service request: {}() with parameters: {}",
				methodName, (parameters == null) ? EMPTY_PARAMETERS : Arrays.toString(parameters));
	}

	public void logVoidRequestFinish(Logger logger, String methodName) {
		logVoidRequestFinish(logger, methodName, (Object[]) null);
	}

	public void logVoidRequestFinish(Logger logger, String methodName, Object... parameters) {
		logRequestFinish(logger, methodName, null, true, parameters);
	}

	public void logRequestFinish(Logger logger, String methodName, Object result) {
		logRequestFinish(logger, methodName, result, (Object[]) null);
	}

	public void logRequestFinish(Logger logger, String methodName, Object result, Object... parameters) {
		logRequestFinish(logger, methodName, result, false, parameters);
	}

	private void logRequestFinish(Logger logger, String methodName, Object result, boolean wasCallVoid, Object... parameters) {
		logger.info("Finished service request: {}() with parameters: {} {}",
				methodName, (parameters == null) ? EMPTY_PARAMETERS : Arrays.toString(parameters), (wasCallVoid ? " for void result" : " for result: " + result));
	}
}
