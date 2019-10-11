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

@SpringBootApplication
@ComponentScan(basePackages = {"no.siriuslabs.computationapi.implementation", "no.siriuslabs.computationapi.demo"})
public class DemoImplementationApplication extends AbstractImplementationApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(DemoImplementationApplication.class);

	private final ConfigProperties configProperties;

	private ApplicationContext ctx;

	public static void main(String[] args) {
		SpringApplication.run(DemoImplementationApplication.class, args);
	}

	@Autowired
	public DemoImplementationApplication(ApplicationContext ctx, ConfigProperties configProperties) {
		super(configProperties);
		this.ctx = ctx;
		this.configProperties = configProperties;
	}

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

	@PreDestroy
	@Profile("!test")
	private void unregisterOnShutdown() throws URISyntaxException, UnknownHostException {
		unregisterWithController();
	}

}
