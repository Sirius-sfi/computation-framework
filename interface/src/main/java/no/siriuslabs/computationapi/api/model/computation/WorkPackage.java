package no.siriuslabs.computationapi.api.model.computation;

import java.util.Map;

public class WorkPackage {

	private DomainType domain;
	private long runId;
	private long id;

	private Map<String, Object> data;

	/**
	 * Constructor needed for de-serialization.
	 */
	public WorkPackage() {
	}

	public WorkPackage(DomainType domain, long id) {
		this.domain = domain;
		this.id = id;
	}

	public DomainType getDomain() {
		return domain;
	}

	public void setDomain(DomainType domain) {
		this.domain = domain;
	}

	public long getRunId() {
		return runId;
	}

	public void setRunId(long runId) {
		this.runId = runId;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "WorkPackage{" +
				"domain=" + domain +
				", id=" + id +
				", data=" + data +
				'}';
	}
}
