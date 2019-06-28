package no.siriuslabs.computationapi.config;

public class PingTimer {

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
