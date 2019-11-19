package no.siriuslabs.computationapi.implementation;

import no.siriuslabs.computationapi.api.model.computation.ComputationResult;
import no.siriuslabs.computationapi.api.model.computation.ResultsProtocol;
import no.siriuslabs.computationapi.api.model.computation.WorkPackage;
import no.siriuslabs.computationapi.api.model.computation.WorkPackageResult;
import no.siriuslabs.computationapi.api.model.request.ComputationRequest;
import no.siriuslabs.computationapi.api.model.request.Payload;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Interface specifying the API methods a worker node (Rest-)controller has to implement.
 * These methods basically show the four phases of a computation run: validate, prepare, compute and collect results.
 */
public interface ImplementationController {

	/**
	 * Validates if the contents of the given Payload is correct in a domain specific way and returns a list of error messages or an empty list if everything is OK.
	 */
	ResponseEntity<List<String>> validateData(Payload payload);

	/**
	 * Prepares the data contained in the given ComputationRequest, creates all WorkPackages needed to solve the request in the most effective way and returns a list of these.
	 */
	ResponseEntity<List<WorkPackage>> prepareAndPackageData(ComputationRequest request);

	/**
	 * Runs the computation of the single given WorkPackage on this worker node and returns the corresponding WorkPackageResult when finished.
	 */
	ResponseEntity<WorkPackageResult> runComputation(WorkPackage workPackage);

	/**
	 * Accumulates all the data from the given ResultsProtocol in a domain specific way and returns a matching ComputationResult to end this computation run.
	 */
	ResponseEntity<ComputationResult> accumulateResults(ResultsProtocol protocol);

}
