package no.siriuslabs.computationapi.controller;

import no.siriuslabs.computationapi.api.exception.InvalidParameterException;
import no.siriuslabs.computationapi.api.model.computation.DomainType;
import org.slf4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.util.Arrays;

public final class ControllerHelper {

	private static final String[] EMPTY_PARAMETERS = {};

	public static final String PARAMETER_MUST_NOT_BE_EMPTY_TEXT = "Parameter must not be empty";
	public static final String UNKNOWN_OR_UNSUPPORTED_DOMAIN_TEXT = "Unknown or unsupported domain ";

	private ControllerHelper() {
	}

	public static void checkParameter(String parameter) {
		if(parameter == null || parameter.trim().isEmpty()) {
			throw new InvalidParameterException(PARAMETER_MUST_NOT_BE_EMPTY_TEXT);
		}
	}

	public static DomainType getDomainTypeFromParameter(String domain) {
		if(domain == null) {
			throw new InvalidParameterException(UNKNOWN_OR_UNSUPPORTED_DOMAIN_TEXT + domain);
		}

		try {
			return DomainType.valueOf(domain.toUpperCase());
		}
		catch(IllegalArgumentException e) {
			throw new InvalidParameterException(UNKNOWN_OR_UNSUPPORTED_DOMAIN_TEXT + domain);
		}
	}

	public static HttpEntity<?> createHttpEntity(Object parameterData) {
		HttpHeaders headers = new HttpHeaders();
		return new HttpEntity<>(parameterData, headers);
	}

	public static void logRequestStart(Logger logger, String methodName) {
		logRequestStart(logger, methodName, (Object[]) null);
	}

	public static void logRequestStart(Logger logger, String methodName, Object... parameters) {
		logger.info("Starting service request: {}() with parameters: {}",
				methodName, (parameters == null) ? EMPTY_PARAMETERS : Arrays.toString(parameters));
	}

	public static void logVoidRequestFinish(Logger logger, String methodName) {
		logVoidRequestFinish(logger, methodName, (Object[]) null);
	}

	public static void logVoidRequestFinish(Logger logger, String methodName, Object... parameters) {
		logRequestFinish(logger, methodName, null, true, parameters);
	}

	public static void logRequestFinish(Logger logger, String methodName, Object result) {
		logRequestFinish(logger, methodName, result, (Object[]) null);
	}

	public static void logRequestFinish(Logger logger, String methodName, Object result, Object... parameters) {
		logRequestFinish(logger, methodName, result, false, parameters);
	}

	private static void logRequestFinish(Logger logger, String methodName, Object result, boolean wasCallVoid, Object... parameters) {
		logger.info("Finished service request: {}() with parameters: {} {}",
				methodName, (parameters == null) ? EMPTY_PARAMETERS : Arrays.toString(parameters), (wasCallVoid ? " for void result" : " for result: " + result));
	}

}
