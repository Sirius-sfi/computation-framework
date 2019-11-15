package no.siriuslabs.computationapi.api.model.node;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import no.siriuslabs.computationapi.api.model.computation.DomainType;
import no.siriuslabs.computationapi.api.model.computation.DomainTypeToStringConverter;
import no.siriuslabs.computationapi.api.model.computation.StringToDomainTypeConverter;

import java.net.URI;
import java.util.Date;

/**
 * Container class representing a single worker node and its current state.<p>
 * The class contains information about the node's unique ID, the DomainType it can support and the URI it can be reached at.
 * The controller also uses it to keep track of the node's current state and when this state last changed.
 */
public class WorkerNode {

	/**
	 * The node's unique identifier.
	 */
	private String id;

	/**
	 * The DomainType this node can support.
	 */
	@JsonSerialize(converter = DomainTypeToStringConverter.class)
	@JsonDeserialize(converter = StringToDomainTypeConverter.class)
	private DomainType domainType;

	/**
	 * The URI which the controller should use to contact this node.
	 */
	private URI uri;

	/**
	 * This node's current status from the controller's point of view.
	 */
	private transient NodeStatus status = NodeStatus.UNAVAILABLE;
	/**
	 * This node's last status change from the controller's point of view.
	 */
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
