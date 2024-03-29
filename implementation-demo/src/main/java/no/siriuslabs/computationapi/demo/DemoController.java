package no.siriuslabs.computationapi.demo;

import no.siriuslabs.computationapi.api.model.computation.ComputationResult;
import no.siriuslabs.computationapi.api.model.computation.ResultsProtocol;
import no.siriuslabs.computationapi.api.model.computation.Status;
import no.siriuslabs.computationapi.api.model.computation.WorkPackage;
import no.siriuslabs.computationapi.api.model.computation.WorkPackageResult;
import no.siriuslabs.computationapi.api.model.request.ComputationRequest;
import no.siriuslabs.computationapi.api.model.request.Payload;
import no.siriuslabs.computationapi.implementation.AbstractImplementationController;
import no.siriuslabs.computationapi.implementation.ImplementationController;
import no.siriuslabs.computationapi.implementation.config.ConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rest controller implementation for the DEmo application.<p>
 * This contains the domain specific worker node API implementation required for ImplementationControllers for the Demo domain.<p>
 * The Demo application accepts data to "calculate" as amounts that are multiplied with a multiplier to illustrate how the system handles variable data packages.
 */
@RestController
public class DemoController extends AbstractImplementationController implements ImplementationController {

	private static final Logger LOGGER = LoggerFactory.getLogger(DemoController.class);

	public static final String VALID_PROPERTY_MESSAGE = "'valid' property must be 'true' to be valid";
	public static final String VALID_KEY = "valid";

	public static final String CALC_KEY = "calc";
	public static final String CALC_EMPTY_MESSAGE = "Calc is missing from dataset or empty";
	public static final String AMOUNT_KEY = "amount";
	public static final String MULTIPLIER_KEY = "multiplier";
	public static final String AMOUNT_EMPTY_MESSAGE = "Amount is missing from dataset";
	public static final String MULTIPLIER_EMPTY_MESSAGE = "Multiplier is missing from dataset";

	public static final String RESULT_KEY = "result";
	public static final String AVG_AMOUNT_KEY = "avgAmount";
	public static final String AVG_MULTIPLIER_KEY = "avgMultiplier";
	public static final String AVG_RESULT_KEY = "avgResult";

	/**
	 * Autowired constructor.
	 */
	@Autowired
	public DemoController(ConfigProperties configProperties) {
		super(configProperties);
	}

	// TODO receive ping

	/**
	 * Implements the data validation step of the Demo application.<p>
	 * The method checks if some required values are present in the Payload coming through the webservice, so that the demo calculations can be made.
	 * It also checks if a property referred to by the constant VALID_KEY is present and if so, if the value is true. This is just used for demo purposes.
	 * @return A list of error messages or an empty list if no errors were found.
	 */
	@PostMapping("/validateData")
	public ResponseEntity<List<String>> validateData(@RequestBody Payload payload) {
		LOGGER.info("Received data package for validation: {}", payload);

		List<String> messages = new ArrayList<>();

		// validate fields in dataset
		validateFields(payload.getData(), messages, payload.getData().keySet());

		// validate required fields
		validateFields(payload.getData(), messages, Arrays.asList(CALC_KEY));

		// validate contents of calc structure
		List<Map<String, Object>> data = (List<Map<String, Object>>) payload.getData().get(CALC_KEY);
		if(data != null) {
			for(Map<String, Object> map : data) {
				validateFields(map, messages, Arrays.asList(AMOUNT_KEY, MULTIPLIER_KEY));
			}
		}

		LOGGER.info("Validation finished. {} messages", messages.size());

		return ResponseEntity.ok(messages);
	}

	/**
	 * Validates each of the given fieldNamesToValidate in the given data structure map. If the validation is not successful, if adds the resulting error message to the given list messages.
	 */
	private void validateFields(Map<String, Object> map, List<String> messages, Collection<String> fieldNamesToValidate) {
		for(String key : fieldNamesToValidate) {
			String message = validate(key, map.get(key));
			if(!message.trim().isEmpty()) {
				messages.add(message);
			}
		}
	}

	/**
	 * Validates a given key-value-pair. Validation logic is independently implemented per key.
	 * If the validation is not successful, it returns an error message otherwise and empty String.
	 */
	protected String validate(String key, Object value) {
		if(VALID_KEY.equals(key)) {
			if(!Boolean.TRUE.equals(Boolean.parseBoolean((String) value))) {
				return VALID_PROPERTY_MESSAGE;
			}
		}
		else if(CALC_KEY.equals(key)) {
			if(value == null) {
				return CALC_EMPTY_MESSAGE;
			}
		}
		else if(AMOUNT_KEY.equals(key)) {
			if(value == null || ((String)value).trim().isEmpty()) {
				return AMOUNT_EMPTY_MESSAGE;
			}
		}
		else if(MULTIPLIER_KEY.equals(key)) {
			if(value == null || ((String)value).trim().isEmpty()) {
				return MULTIPLIER_EMPTY_MESSAGE;
			}
		}

		return "";
	}

	/**
	 * Implements the data preparation and WorkPackage generation step of the Demo application.<p>
	 * This method creates a WorkPackage for every amount-multiplier pair found in the calc data structure. To simulate a more complex process, it waits some time until the result is returned.
	 * @return The list of WorkPackages generated from the data in the ComputationRequest.
	 */
	@PostMapping("/prepareAndPackageData")
	public ResponseEntity<List<WorkPackage>> prepareAndPackageData(@RequestBody ComputationRequest request) {
		LOGGER.info("Received data package for preparation: {}", request);

		try {
			final int delay = 15000;
			LOGGER.info("Preparing data for " + delay / 1000 + "s...");
			Thread.sleep(delay);
			LOGGER.info("Preparing data is done");
		}
		catch(InterruptedException e) {
			LOGGER.error(e.getMessage(), e);
		}

		List<Map<String, Object>> calc = (List<Map<String, Object>>) request.getPayload().getData().get(CALC_KEY);
		List<WorkPackage> results = new ArrayList<>(calc.size());

		for(Map<String, Object> calcEntry : calc) {
			WorkPackage wp = new WorkPackage(request.getDomain(), getNextWorkPackageId());
			wp.setData(calcEntry);

			results.add(wp);
		}

		LOGGER.info("Preparation finished. Returned results: {}", results);
		return ResponseEntity.ok(results);
	}

	/**
	 * Implements the computation step of the Demo application.<p>
	 * For demo purposes the method calculates the result of the amount and multiplier values found in the given WorkPackage. To simulate a more complex process, it waits some time until the result is returned.
	 * @return A WorkPackageResult containing the resulting value of the multiplication.
	 */
	@PostMapping("/runComputation")
	public ResponseEntity<WorkPackageResult> runComputation(@RequestBody WorkPackage workPackage) {
		LOGGER.info("Received data package for computation: {}", workPackage);

		try {
			final int delay = 60000;
			LOGGER.info("Computing package {} for " + delay / 1000 + "s...", workPackage.getId());
			Thread.sleep(delay);
			LOGGER.info("Computing package {} is done", workPackage.getId());
		}
		catch(InterruptedException e) {
			LOGGER.error(e.getMessage(), e);
		}

		long amount = Long.parseLong((String)workPackage.getData().get(AMOUNT_KEY));
		long multiplier = Long.parseLong((String)workPackage.getData().get(MULTIPLIER_KEY));
		long calcResult = amount * multiplier;

		Map<String, Object> resultData = new HashMap<>();
		resultData.put(RESULT_KEY, calcResult);

		WorkPackageResult result = new WorkPackageResult(workPackage);
		result.setData(resultData);

		LOGGER.info("Computation finished. Returned result: {}", result);
		return ResponseEntity.ok(result);
	}

	/**
	 * Implements the results accumulation of the Demo application.<p>
	 * The method inspects all WorkPackageResults of the computation run and calculates average values for amount, multiplier and calculation result for demo purposes.
	 * It also adds statistical information about the computation run (provided by AbstractImplementationController).
	 * @return ComputationResult containing calculated values and statistics.
	 */
	@PostMapping("/accumulateResults")
	public ResponseEntity<ComputationResult> accumulateResults(@RequestBody ResultsProtocol protocol) {
		LOGGER.info("Calculating averages from {} results", protocol.getWorkPackageResults().size());

		long avgAmount = 0;
		long avgMultiplier = 0;
		long avgCalcResult = 0;

		for(WorkPackageResult workPackageResult : protocol.getWorkPackageResults()) {
			long amount = Long.parseLong((String)workPackageResult.getWorkPackage().getData().get(AMOUNT_KEY));
			long multiplier = Long.parseLong((String)workPackageResult.getWorkPackage().getData().get(MULTIPLIER_KEY));
			long calcResult = ((Integer)workPackageResult.getData().get(RESULT_KEY)).longValue();

			avgAmount += amount;
			avgMultiplier += multiplier;
			avgCalcResult += calcResult;
		}

		long numberOfResults = protocol.getWorkPackageResults().size();
		avgAmount = avgAmount / numberOfResults;
		avgMultiplier = avgMultiplier / numberOfResults;
		avgCalcResult = avgCalcResult / numberOfResults;
		LOGGER.info("Average amount was {}, average multiplier {} and average result {}", avgAmount, avgMultiplier, avgCalcResult);

		Map<String, Object> resultData = new HashMap<>();
		resultData.put(AVG_AMOUNT_KEY, avgAmount);
		resultData.put(AVG_MULTIPLIER_KEY, avgMultiplier);
		resultData.put(AVG_RESULT_KEY, avgCalcResult);

		addTimingResults(protocol, resultData);

		final ComputationResult result = new ComputationResult(Status.DONE, resultData);
		LOGGER.info("Result accumulation finished. Returned result: {}", result);
		return ResponseEntity.ok().body(result);
	}

}
