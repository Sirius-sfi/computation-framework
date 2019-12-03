package no.siriuslabs.computationapi.controller;

import no.siriuslabs.computationapi.api.exception.InvalidParameterException;
import no.siriuslabs.computationapi.api.model.computation.DomainType;
import no.siriuslabs.computationapi.api.model.computation.DomainTypeImpl;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

/**
 * Helper class offering functionality for controller side Rest controllers such as parameter checks and DomainType conversion.
 */
public final class ControllerHelper {
	
	public static final String PARAMETER_MUST_NOT_BE_EMPTY_TEXT = "Parameter must not be empty";
	public static final String DOMAIN_MUST_NOT_BE_EMPTY_TEXT = "Domain parameter must not be empty";

	/**
	 * Default constructor.
	 */
	private ControllerHelper() {
	}

	/**
	 * Checks if a String parameter (usually coming in via a webservice) is null or empty. If so the method throws an InvalidParameterException.
	 */
	public static void checkParameter(String parameter) {
		if(parameter == null || parameter.trim().isEmpty()) {
			throw new InvalidParameterException(PARAMETER_MUST_NOT_BE_EMPTY_TEXT);
		}
	}

	/**
	 * Encapsulates the String representation of a DomainType into a DomainType instance for use on the controller side.<p>
	 * As the domain specific implementations used on the worker node side are unknown here, the controller side uses a generic implementation based on the String representation.
	 */
	public static DomainType getDomainTypeFromParameter(String domain) {
		if(domain == null) {
			throw new InvalidParameterException(DOMAIN_MUST_NOT_BE_EMPTY_TEXT + domain);
		}

		return new DomainTypeImpl(domain.toUpperCase());
	}

	/**
	 * Creates and returns a new HttpEntity based on the parameterData object given to the method.
	 */
	public static HttpEntity<?> createHttpEntity(Object parameterData) {
		HttpHeaders headers = new HttpHeaders();
		return new HttpEntity<>(parameterData, headers);
	}

}
