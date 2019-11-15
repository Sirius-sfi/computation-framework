package no.siriuslabs.computationapi.api.model.config;

/**
 * Configuration container class representing a Timer in Spring config files.<p>
 * Used on the controller side only (but known by Controller and thus in this module).
 */
public class Timer {

	/**
	 * Value for the startup delay of a timer. The first execution will start after the time given here.
	 */
	private long startupDelay;

	/**
	 * Value for the call interval of a timer. Invocations will be separated by the amount of time defined here.
	 */
	private long callInterval;

	public long getStartupDelay() {
		return startupDelay;
	}

	public void setStartupDelay(long startupDelay) {
		this.startupDelay = startupDelay;
	}

	public long getCallInterval() {
		return callInterval;
	}

	public void setCallInterval(long callInterval) {
		this.callInterval = callInterval;
	}
}
