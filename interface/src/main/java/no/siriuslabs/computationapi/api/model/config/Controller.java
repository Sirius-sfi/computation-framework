package no.siriuslabs.computationapi.api.model.config;

import java.net.URI;

public class Controller {

	private URI localUrl;
	private URI dockerUrl;
	private int retryCount;
	private long retryDelay;
	private Timer timer;

	public URI getLocalUrl() {
		return localUrl;
	}

	public void setLocalUrl(URI localUrl) {
		this.localUrl = localUrl;
	}

	public URI getDockerUrl() {
		return dockerUrl;
	}

	public void setDockerUrl(URI dockerUrl) {
		this.dockerUrl = dockerUrl;
	}

	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public long getRetryDelay() {
		return retryDelay;
	}

	public void setRetryDelay(long retryDelay) {
		this.retryDelay = retryDelay;
	}

	public Timer getTimer() {
		return timer;
	}

	public void setTimer(Timer timer) {
		this.timer = timer;
	}
}
