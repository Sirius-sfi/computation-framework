package no.siriuslabs.computationapi.implementation.config;

import no.siriuslabs.computationapi.api.model.config.Controller;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "config")
public class ConfigProperties {

	private Controller controller;
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
