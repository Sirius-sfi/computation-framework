package no.siriuslabs.computationapi.demo;

import no.siriuslabs.computationapi.implementation.AbstractImplementationApplication;
import no.siriuslabs.computationapi.implementation.config.ConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;

import javax.annotation.PreDestroy;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

/**
 * Demo project Spring application.<p>
 * Extends AbstractImplementationApplication and mostly uses its basic functionality. This implementation adds only the necessary Spring annotations, injections
 * and a CommandLineRunner and PreDestroy method for registration and de-registration with the controller.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"no.siriuslabs.computationapi.implementation", "no.siriuslabs.computationapi.demo"})
public class DemoImplementationApplication extends AbstractImplementationApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(DemoImplementationApplication.class);

	/**
	 * Spring configuration with contents of config file.
	 */
	private final ConfigProperties configProperties;

	/**
	 * Spring application context.
	 */
	private ApplicationContext ctx;

	/**
	 * Main method.<p>
	 * Possible program arguments: none<p>
	 * Possible system properties: -Ddocker=true if used in a Docker container
	 */
	public static void main(String[] args) {
		SpringApplication.run(DemoImplementationApplication.class, args);
	}

	/**
	 * Autowired constructor.
	 */
	@Autowired
	public DemoImplementationApplication(ApplicationContext ctx, ConfigProperties configProperties) {
		super(configProperties);
		this.ctx = ctx;
		this.configProperties = configProperties;
	}

	/**
	 * CommandLineRunner that is executed a application startup to register this worker node with the controller.
	 */
	@Bean
	@Profile("!test")
	public CommandLineRunner run() {
		return (String... args) -> {
			LOGGER.info("Registering node...");
			boolean success = registerWithController();
			if(!success) {
				LOGGER.error("Registering with controller failed - shutting down");
				SpringApplication.exit(ctx, () -> 1);
			}
		};
	}

	/**
	 * Method that is called on application shutdown to de-register this worker node with the controller.
	 */
	@PreDestroy
	@Profile("!test")
	private void unregisterOnShutdown() throws URISyntaxException, UnknownHostException {
		unregisterWithController();
	}

}
