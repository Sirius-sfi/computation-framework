package no.siriuslabs.computationapi.api.model.computation;

import com.fasterxml.jackson.databind.util.StdConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spring converter class used to convert DomainType instances into a string representation during serialization.
 */
public class DomainTypeToStringConverter extends StdConverter<DomainType, String> {

	@Override
	public String convert(DomainType d) {
		return d.getDomainType();
	}
}
