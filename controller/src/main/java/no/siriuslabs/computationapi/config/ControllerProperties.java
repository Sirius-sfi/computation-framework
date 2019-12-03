package no.siriuslabs.computationapi.config;

import no.siriuslabs.computationapi.api.model.config.Controller;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration container class representing the top-most level of the configuration file structure in regards to controller configuration.
 * It contains a controller element which provides more details.
 */
@Component
@ConfigurationProperties(prefix = "config")
public class ControllerProperties {

	/**
	 * Controller object containing URL, retry and timer configurations.
	 */
	private Controller controller;

	public Controller getController() {
		return controller;
	}

	public void setController(Controller controller) {
		this.controller = controller;
	}
}
