package no.siriuslabs.computationapi.event;

import no.siriuslabs.computationapi.api.model.computation.DomainType;
import no.siriuslabs.computationapi.api.model.computation.WorkPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DataPreparartionFinishedEvent extends AbstractDataWorkflowEvent {

	private static final Logger LOGGER = LoggerFactory.getLogger(DataPreparartionFinishedEvent.class);

	private final DomainType domain;
	private final List<WorkPackage> workPackages;

	public DataPreparartionFinishedEvent(Object source, DomainType domain, List<WorkPackage> workPackages) {
		super(source);
		this.domain = domain;
		this.workPackages = workPackages;
	}

	public DomainType getDomain() {
		return domain;
	}

	public List<WorkPackage> getWorkPackages() {
		return workPackages;
	}

	@Override
	public String toString() {
		return "DataPreparartionFinishedEvent{" +
				"domain=" + domain +
				", workPackages=" + workPackages +
				'}';
	}
}
