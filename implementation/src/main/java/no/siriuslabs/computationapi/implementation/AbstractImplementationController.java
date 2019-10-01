package no.siriuslabs.computationapi.implementation;

import no.siriuslabs.computationapi.api.model.computation.ResultsProtocol;
import no.siriuslabs.computationapi.implementation.config.ConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractImplementationController implements ImplementationController {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractImplementationController.class);

	private final ConfigProperties configProperties;

	private long idCounter = 0;

	protected AbstractImplementationController(ConfigProperties configProperties) {
		this.configProperties = configProperties;
	}

	protected long getNextId() {
		return idCounter += 1;
	}

	protected void addTimingResults(@RequestBody ResultsProtocol protocol, Map<String, Object> resultData) {
		Map<String, Object> timingData = new HashMap<>();
		timingData.put("computingTime", protocol.getFinishedTimestamp() - protocol.getStartedTimestamp());
		timingData.put("preparationTime", protocol.getPreparationTime());
		timingData.put("fastestWP", protocol.getMinWpTime());
		timingData.put("slowestWP", protocol.getMaxWpTime());
		timingData.put("avgWP", protocol.getAvgWpTime());

		resultData.put("timingData", timingData);
	}

}
