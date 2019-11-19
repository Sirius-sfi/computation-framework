package no.siriuslabs.computationapi.implementation;

import no.siriuslabs.computationapi.api.model.computation.ResultsProtocol;
import no.siriuslabs.computationapi.implementation.config.ConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract superclass for worker node Rest controllers.<p>
 * It provides shared functionality such as managing generation of WorkPackage unique IDs and adding timing metadata the results.<p>
 * These convenience methods still have to be called by the concrete implementation to be used!
 */
public abstract class AbstractImplementationController implements ImplementationController {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractImplementationController.class);

	/**
	 * Spring configuration with contents of config file.
	 */
	private final ConfigProperties configProperties;

	/**
	 * Counter of WorkPackage unique IDs.
	 */
	private long packageIdCounter = 0;

	/**
	 * Constructor accepting the configuration object (to be injected into the concrete implementation class).
	 */
	protected AbstractImplementationController(ConfigProperties configProperties) {
		this.configProperties = configProperties;
	}

	/**
	 * Returns the next free WorkPackage ID.
	 */
	protected long getNextWorkPackageId() {
		return packageIdCounter += 1;
	}

	/**
	 * Adds timing metadata taken from the given ResultsProtocol into a sub-structure of the given Map called "timingData".
	 */
	protected void addTimingResults(@RequestBody ResultsProtocol protocol, Map<String, Object> resultData) {
		Map<String, Object> timingData = new HashMap<>();
		timingData.put("computingTime", protocol.getFinishedTimestamp() - protocol.getStartedTimestamp());
		timingData.put("preparationTime", protocol.getPreparationTime());
		timingData.put("numberNodesStart", protocol.getNumberNodesStart());
		timingData.put("numberNodesEnd", protocol.getNumberNodesEnd());
		timingData.put("numberWPs", protocol.getNumberWPs());
		timingData.put("fastestWP", protocol.getMinWpTime());
		timingData.put("slowestWP", protocol.getMaxWpTime());
		timingData.put("avgWP", protocol.getAvgWpTime());

		resultData.put("timingData", timingData);
	}

}
