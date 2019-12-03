package no.siriuslabs.computationapi.config;

/**
 * Configuration container class representing a PingTimer configuration including startup delay and ping interval.
 */
public class PingTimer {

	/**
	 * Delay between application startup and the first execution of the timer.
	 */
	private long startupDelay;
	/**
	 * Interval between two calls of the timer.
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
