package no.siriuslabs.computationapi.controller;

import no.siriuslabs.computationapi.api.exception.InvalidParameterException;
import no.siriuslabs.computationapi.api.model.computation.DomainType;
import no.siriuslabs.computationapi.api.model.computation.DomainTypeImpl;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

public final class ControllerHelper {
	
	public static final String PARAMETER_MUST_NOT_BE_EMPTY_TEXT = "Parameter must not be empty";
	public static final String DOMAIN_MUST_NOT_BE_EMPTY_TEXT = "Domain parameter must not be empty";

	private ControllerHelper() {
	}

	public static void checkParameter(String parameter) {
		if(parameter == null || parameter.trim().isEmpty()) {
			throw new InvalidParameterException(PARAMETER_MUST_NOT_BE_EMPTY_TEXT);
		}
	}

	public static DomainType getDomainTypeFromParameter(String domain) {
		if(domain == null) {
			throw new InvalidParameterException(DOMAIN_MUST_NOT_BE_EMPTY_TEXT + domain);
		}

		return new DomainTypeImpl(domain.toUpperCase());
	}

	public static HttpEntity<?> createHttpEntity(Object parameterData) {
		HttpHeaders headers = new HttpHeaders();
		return new HttpEntity<>(parameterData, headers);
	}

}
