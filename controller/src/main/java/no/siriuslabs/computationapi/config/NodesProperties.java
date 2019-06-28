package no.siriuslabs.computationapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "nodes")
public class NodesProperties {

	private PingTimer pingTimer;

	public PingTimer getPingTimer() {
		return pingTimer;
	}

	public void setPingTimer(PingTimer pingTimer) {
		this.pingTimer = pingTimer;
	}
}
