package no.siriuslabs.computationapi.api.model.computation;

import java.util.Map;

public class WorkPackage {

	private final DomainType domain;
	private final long id;

	private Map<String, Object> data;

	/**
	 * Constructor needed for de-serialization.
	 */
	public WorkPackage(DomainType domain, long id) {
		this.domain = domain;
		this.id = id;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	public DomainType getDomain() {
		return domain;
	}

	public long getId() {
		return id;
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
