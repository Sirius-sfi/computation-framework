package no.siriuslabs.computationapi.api.model.config;

public class Timer {

	private long startupDelay;
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
