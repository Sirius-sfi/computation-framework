package no.siriuslabs.computationapi.api.model.computation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Map;
import java.util.Objects;

/**
 * Container class representing a single work package.<p>
 * A WorkPackage contains information about the DomainType and the ID of the computation run it belongs to, its own unique ID and a collection of domain specific data.
 */
public class WorkPackage {

	/**
	 * Domain type the computation run belongs to.
	 */
	@JsonSerialize(converter = DomainTypeToStringConverter.class)
	@JsonDeserialize(converter = StringToDomainTypeConverter.class)
	private DomainType domain;

	/**
	 * ID of the computation run this package belongs to.
	 */
	private long runId;

	/**
	 * This WorkPackage's unique identifier.
	 */
	private long id;

	/**
	 * Map containing domain specific data needed for the computations.
	 */
	private Map<String, Object> data;

	/**
	 * Constructor needed for de-serialization.
	 */
	public WorkPackage() {
	}

	/**
	 * Constructor accepting DomainType and work package ID.
	 */
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
	public boolean equals(Object o) {
		if(this == o) {
			return true;
		}
		if(o == null || getClass() != o.getClass()) {
			return false;
		}
		WorkPackage that = (WorkPackage) o;
		return runId == that.runId &&
				id == that.id &&
				Objects.equals(domain, that.domain);
	}

	@Override
	public int hashCode() {
		return Objects.hash(domain, runId, id);
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
