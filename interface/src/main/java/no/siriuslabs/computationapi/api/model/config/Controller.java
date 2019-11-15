package no.siriuslabs.computationapi.api.model.config;

import java.net.URI;

/**
 * Configuration container class representing a controller in Spring config files on both controller and implementation (WorkerNode) sides.
 */
public class Controller {

	/**
	 * Server URL when used locally (e.g. from IDE) without using Docker.<p>
	 * <b>Implementation side only!</b>
	 */
	private URI localUrl;
	/**
	 * Server URL when used with Docker.<p>
	 * <b>Implementation side only!</b>
	 */
	private URI dockerUrl;

	/**
	 * Number of retry attempts to be taken for various action before failing.
	 */
	private int retryCount;
	/**
	 * Delay between one retry attempt and the next try.
	 */
	private long retryDelay;

	/**
	 * Timer related configuration data.<p>
	 * <b>Controller side only!</b>
	 */
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
