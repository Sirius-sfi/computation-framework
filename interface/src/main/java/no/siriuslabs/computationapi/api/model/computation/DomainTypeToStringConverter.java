package no.siriuslabs.computationapi.api.model.computation;

import com.fasterxml.jackson.databind.util.StdConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DomainTypeToStringConverter extends StdConverter<DomainType, String> {

	private static final Logger LOGGER = LoggerFactory.getLogger(DomainTypeToStringConverter.class);

	@Override
	public String convert(DomainType d) {
		return d.getDomainType();
	}
}
