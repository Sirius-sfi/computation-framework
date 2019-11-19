package no.siriuslabs.computationapi.implementation.config;

import no.siriuslabs.computationapi.api.model.config.Controller;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration container class representing the top-most level of the configuration file structure.
 * It contains Controller and Node elements providing more details.
 */
@Component
@ConfigurationProperties(prefix = "config")
public class ConfigProperties {

	/**
	 * Controller object containing URL, retry and timer configurations.
	 */
	private Controller controller;

	/**
	 * Node object containing DomainType information.
	 */
	private Node node;

	public Controller getController() {
		return controller;
	}

	public void setController(Controller controller) {
		this.controller = controller;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}
}
