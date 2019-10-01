package no.siriuslabs.computationapi.implementation;

import no.siriuslabs.computationapi.api.model.computation.ComputationResult;
import no.siriuslabs.computationapi.api.model.computation.ResultsProtocol;
import no.siriuslabs.computationapi.api.model.computation.WorkPackage;
import no.siriuslabs.computationapi.api.model.computation.WorkPackageResult;
import no.siriuslabs.computationapi.api.model.request.ComputationRequest;
import no.siriuslabs.computationapi.api.model.request.Payload;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ImplementationController {

	ResponseEntity<List<String>> validateData(Payload payload);

	ResponseEntity<List<WorkPackage>> prepareAndPackageData(ComputationRequest request);

	ResponseEntity<WorkPackageResult> runComputation(WorkPackage workPackage);

	ResponseEntity<ComputationResult> accumulateResults(ResultsProtocol protocol);

}
