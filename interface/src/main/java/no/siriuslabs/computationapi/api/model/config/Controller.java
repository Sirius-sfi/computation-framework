package no.siriuslabs.computationapi.api.model.config;

import java.net.URI;

public class Controller {

	private URI url;
	private int retryCount;
	private long retryDelay;
	private Timer timer;

	public URI getUrl() {
		return url;
	}

	public void setUrl(URI url) {
		this.url = url;
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
