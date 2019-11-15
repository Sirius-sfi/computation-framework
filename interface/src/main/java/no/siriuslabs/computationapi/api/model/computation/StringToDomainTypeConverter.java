package no.siriuslabs.computationapi.api.model.computation;

import com.fasterxml.jackson.databind.util.StdConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spring converter class used to convert a string representation of a DomainType into DomainType instances during deserialization.
 */
public class StringToDomainTypeConverter extends StdConverter<String, DomainType> {

	@Override
	public DomainType convert(String s) {
		return new DomainTypeImpl(s);
	}
}
