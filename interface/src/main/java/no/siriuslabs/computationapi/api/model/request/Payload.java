package no.siriuslabs.computationapi.api.model.request;

import java.util.Map;

public class Payload {

	Map<String, Object> data;

	/**
	 * Constructor needed for de-serialization.
	 */
	public Payload() {
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "Payload{" +
				"data=" + data +
				'}';
	}
}
