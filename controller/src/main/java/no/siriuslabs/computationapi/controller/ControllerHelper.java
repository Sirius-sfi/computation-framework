package no.siriuslabs.computationapi.controller;

import no.siriuslabs.computationapi.api.exception.InvalidParameterException;
import no.siriuslabs.computationapi.api.model.computation.DomainType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

public final class ControllerHelper {
	
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

}
