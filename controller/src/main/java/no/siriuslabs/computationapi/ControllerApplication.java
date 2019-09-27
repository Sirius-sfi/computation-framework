package no.siriuslabs.computationapi;

import no.siriuslabs.computationapi.api.model.computation.DomainType;
import no.siriuslabs.computationapi.config.ControllerProperties;
import no.siriuslabs.computationapi.config.NodesProperties;
import no.siriuslabs.computationapi.controller.ControllerHelper;
import no.siriuslabs.computationapi.controller.NodeController;
import no.siriuslabs.computationapi.controller.WorkPackageController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@ComponentScan(basePackages = {"no.siriuslabs.computationapi", "no.siriuslabs.computationapi.api"})
@EnableAsync
public class ControllerApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(ControllerApplication.class);

	private final ControllerProperties controllerProperties;
	private final NodesProperties nodesProperties;

	private final WorkPackageController workPackageController;
	private final NodeController nodeController;

	public static void main(String[] args) {
		SpringApplication.run(ControllerApplication.class, args);
	}

	@Autowired
	public ControllerApplication(ControllerProperties controllerProperties, NodeController nodeController, WorkPackageController workPackageController, NodesProperties nodesProperties) {
		this.controllerProperties = controllerProperties;
		this.nodeController = nodeController;
		this.workPackageController = workPackageController;
		this.nodesProperties = nodesProperties;
	}

	@Bean
	@Profile("!test")
	public CommandLineRunner run() {
		return (String... args) -> {
			LOGGER.info("Setting up timers");
			setupPingTimer();
			setupWorkDistributionTimer();
		};
	}

	private void setupPingTimer() {
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				nodeController.pingNodes();
			}
		};

		final long startupDelay = nodesProperties.getPingTimer().getStartupDelay();
		final long callInterval = nodesProperties.getPingTimer().getCallInterval();
		setupTimer("ping timer", timerTask, startupDelay, callInterval);
	}

	private void setupWorkDistributionTimer() {
		String domainName = System.getProperties().getProperty("domain");
		LOGGER.info("Domain set by command line is {}", domainName);
		DomainType domain = ControllerHelper.getDomainTypeFromParameter(domainName);

		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				workPackageController.distributeWork(domain);
			}
		};

		final long startupDelay = controllerProperties.getController().getTimer().getStartupDelay();
		final long callInterval = controllerProperties.getController().getTimer().getCallInterval();
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		ScheduledFuture<?> future = executor.scheduleAtFixedRate(timerTask, startupDelay, callInterval, TimeUnit.MILLISECONDS);
	}

	private void setupTimer(String name, TimerTask timerTask, long startupDelay, long callInterval) {
		Timer timer = new Timer(name);
		timer.schedule(timerTask, startupDelay, callInterval);
		LOGGER.info("Started timer {} with delay={} sec. and interval={} sec.", name, startupDelay / 1000, callInterval / 1000);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
