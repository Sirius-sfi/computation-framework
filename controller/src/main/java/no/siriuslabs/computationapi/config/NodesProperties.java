package no.siriuslabs.computationapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration container class representing the top-most level of the configuration file structure in regards to node ping timer configuration.
 * It contains a controller element which provides more details.
 */
@Component
@ConfigurationProperties(prefix = "nodes")
public class NodesProperties {

	/**
	 * PingTimer object containing delays and intervals of ping calls.
	 */
	private PingTimer pingTimer;

	public PingTimer getPingTimer() {
		return pingTimer;
	}

	public void setPingTimer(PingTimer pingTimer) {
		this.pingTimer = pingTimer;
	}
}
