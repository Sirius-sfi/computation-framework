package no.siriuslabs.computationapi.implementation.config;

import no.siriuslabs.computationapi.api.model.computation.DomainType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Spring converter class used to parse string representations of enum class and element in the config file and convert them into the correct enum element of the right class.<p>
 * The format of the string representation is expected to be: [fully qualified package and class name]:[enum element in that class]<p>
 * If a non enum implementation of DomainType is made, this converter should be extended or replaced.
 */
@Component
@ConfigurationPropertiesBinding
public class ConfigDomainTypeConverter implements Converter<String, DomainType> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigDomainTypeConverter.class);

	@Override
	public DomainType convert(String s) {
		String[] split = s.split(":");
		if(split.length != 2) {
			return null;
		}

		String className = split[0];
		String itemName = split[1];
		try {
			Class<? extends DomainType> enumClass = (Class<? extends DomainType>) Class.forName(className);
			Method method = enumClass.getDeclaredMethod("valueOf", String.class);
			return (DomainType) method.invoke(enumClass, itemName);
		}
		catch(ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			LOGGER.error(e.getMessage(), e);
			return null;
		}
	}
}
