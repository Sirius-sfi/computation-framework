package no.siriuslabs.computationapi.api.model.node;

import no.siriuslabs.computationapi.api.model.computation.DomainType;

import java.net.URI;
import java.util.Date;

public class WorkerNode {

	private String id;
	private DomainType domainType;
	private URI uri;

	private transient NodeStatus status = NodeStatus.UNAVAILABLE;
	private transient Date lastStatusChange;

	/**
	 * Constructor needed for de-serialization.
	 */
	public WorkerNode() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public DomainType getDomainType() {
		return domainType;
	}

	public void setDomainType(DomainType domainType) {
		this.domainType = domainType;
	}

	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	public NodeStatus getStatus() {
		return status;
	}

	public void setStatus(NodeStatus status) {
		this.status = status;
		lastStatusChange = new Date();
	}

	public Date getLastStatusChange() {
		return lastStatusChange == null ? null : (Date) lastStatusChange.clone();
	}

	@Override
	public String toString() {
		return "WorkerNode{" +
				"id='" + id + '\'' +
				", domainType=" + domainType +
				", uri=" + uri +
				", status=" + status +
				'}';
	}
}
