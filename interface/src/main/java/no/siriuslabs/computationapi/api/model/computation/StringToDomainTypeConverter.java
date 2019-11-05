package no.siriuslabs.computationapi.api.model.computation;

import com.fasterxml.jackson.databind.util.StdConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringToDomainTypeConverter extends StdConverter<String, DomainType> {

	private static final Logger LOGGER = LoggerFactory.getLogger(StringToDomainTypeConverter.class);

	@Override
	public DomainType convert(String s) {
		return new DomainTypeImpl(s);
	}
}
