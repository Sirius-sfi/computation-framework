package no.siriuslabs.computationapi.implementation;

import no.siriuslabs.computationapi.api.model.node.WorkerNode;
import no.siriuslabs.computationapi.implementation.config.ConfigProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

/**
 * Abstract superclass for WorkerNode implementation applications.<p>
 * It includes prepared functionality such as registering and unregistering a node with the controller and configuring a WorkerNode object with the correct data.
 * Usually the concrete node-side implementation only needs to extend this class and provide a Spring CommandLineRunner to execute the registration as well as a
 * method annotated with PreDestroy or some kind of other lifecycle listener to notify the controller when the application goes down.
 */
public abstract class AbstractImplementationApplication {

	/**
	 * Enum used to encapsulate information about the "direction" (flavour) of a registration/de-registration service call.
	 * Used this way to be able to share as much of the code doing the actual communication as possible.
	 */
	protected enum RegistrationFlavour {
		REGISTER("registering", "registerNode"),
		UNREGISTER("unregistering", "unregisterNode");

		/**
		 * Description of the activity used in things like logging. This is more of a byproduct.
		 */
		private final String activityString;
		/**
		 * String representing part of the service's URL, depending on direction (flavour).
		 */
		private final String serviceSubPath;

		/**
		 * Enum constructor expecting all elements.
		 */
		RegistrationFlavour(String activityString, String serviceSubPath) {
			this.activityString = activityString;
			this.serviceSubPath = serviceSubPath;
		}

		public String getActivityString() {
			return activityString;
		}

		public String getServiceSubPath() {
			return serviceSubPath;
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractImplementationApplication.class);

	/**
	 * Spring configuration with contents of config file.
	 */
	private final ConfigProperties configProperties;

	/**
	 * Spring RestTemplate to execute Rest communication.
	 */
	private final RestTemplate restTemplate;

	/**
	 * Port this server runs on. Mapped from config file.<p>
	 * Annotated is the default port if nothing is stated in the config file.
	 */
	@Value("${server.port}")
	private String serverPort;

	/**
	 * Constructor accepting the configuration object (to be injected into the concrete implementation class).
	 */
	protected AbstractImplementationApplication(ConfigProperties configProperties) {
		this.configProperties = configProperties;
		this.restTemplate = new RestTemplate();
	}

	/**
	 * Constructor - for testing only!
	 */
	protected AbstractImplementationApplication(ConfigProperties configProperties, RestTemplate restTemplate) {
		this.configProperties = configProperties;
		this.restTemplate = restTemplate;
	}

	/**
	 * Entry point to the registration of this node with the controller.
	 * @throws URISyntaxException 	If one of the generated URIs is a correct URI.
	 * @throws UnknownHostException	If the local host name of this machine could not be resolved into an address.
	 */
	protected boolean registerWithController() throws URISyntaxException, UnknownHostException {
		LOGGER.info("Starting to register with controller");

		WorkerNode node = configureWorkerNode();
		URI uri = createServiceUri(RegistrationFlavour.REGISTER);

		return register(node, uri);
	}

	/**
	 * Configures a WorkerNode object with the data for this machine.
	 * @throws URISyntaxException 	If the generated worker node URI is a correct URI.
	 * @throws UnknownHostException	If the local host name of this machine could not be resolved into an address.
	 */
	protected WorkerNode configureWorkerNode() throws UnknownHostException, URISyntaxException {
		WorkerNode node = new WorkerNode();
		node.setDomainType(configProperties.getNode().getDomain());
		LOGGER.info("Node's domain is {}", node.getDomainType());

		InetAddress inetAddress = InetAddress.getLocalHost();
		final String hostAdressPart = inetAddress.getHostName() == null || inetAddress.getHostName().trim().isEmpty() ? inetAddress.getHostAddress() : inetAddress.getHostName();
		final String ipAndPort = hostAdressPart + ':' + serverPort;
		node.setId(ipAndPort);
		LOGGER.info("Node's IP:port combination is {}", ipAndPort);

		final String url = "http://" + ipAndPort;
		node.setUri(new URI(url));
		return node;
	}

	/**
	 * Creates and returns the complete URI for a registration or de-registration service call to the controller (depending on given RegistrationFlavour).<p>
	 * This also takes into account if this node is running in a Docker environment or not (assuming that the controller will use the same environment).
	 * @throws URISyntaxException If the generated controller URI is a correct URI.
	 */
	protected URI createServiceUri(RegistrationFlavour flavour) throws URISyntaxException {
		final URI controllerUrl;
		if(isDockerActive()) {
			controllerUrl = configProperties.getController().getDockerUrl();
		}
		else {
			controllerUrl = configProperties.getController().getLocalUrl();
		}
		LOGGER.info("Controller's URL is {}", controllerUrl);
		final String serviceUrl = controllerUrl + flavour.getServiceSubPath();
		LOGGER.info("Service URL to be called is {}", serviceUrl);

		return new URI(serviceUrl);
	}

	/**
	 * Returns true if the application runs in a Docker environment (based on a system property that must be set in that case).
	 */
	private boolean isDockerActive() {
		String dockerFlag = System.getProperties().getProperty("docker");
		return dockerFlag == null || dockerFlag.trim().isEmpty() ? false : Boolean.parseBoolean(dockerFlag);
	}

	/**
	 * Tries to register the given WorkerNode at the given controller URI and returns true if successful (answer with HTTP 200).
	 * If the attempt fails, it will retry a number of times (specified in the config file) until successful or return false otherwise.
	 */
	protected boolean register(WorkerNode node, URI uri) {
		HttpHeaders headers = new HttpHeaders();
		HttpEntity<WorkerNode> entity = new HttpEntity<>(node, headers);

		final int maxRetryCount = configProperties.getController().getRetryCount();
		boolean success = false;
		int retryCounter = 0;
		do {
			final HttpStatus result = callRegistrationService(RegistrationFlavour.REGISTER, uri, entity);
			if(HttpStatus.OK == result) {
				LOGGER.info("Registered successfully @ {}", uri);
				success = true;
				break;
			}
			else {
				LOGGER.info("Registering not successful (response code {}) - preparing retry. Counter @ {}", result, retryCounter);
				retryCounter++;
				waitForRetry();
			}
		}
		while(retryCounter <= maxRetryCount);

		if(!success) {
			LOGGER.error("{} attempts to register failed - cancelling", maxRetryCount);
		}

		return success;
	}

	/**
	 * Calls the registration service specified in the URI (either registration or de-registration service) using the given WorkerNode HttpEntity.
	 * The RegistrationFlavour parameter is used for logging only.<p>
	 * Returns the HTTPStatus received from the controller. Checked exceptions are caught and logged. If there is no status code to return, it will return null instead.
	 */
	protected HttpStatus callRegistrationService(RegistrationFlavour flavour, URI uri, HttpEntity<WorkerNode> entity) {
		try {
			ResponseEntity<Object> response = restTemplate.exchange(uri, HttpMethod.POST, entity, Object.class);

			LOGGER.info("Response code={}, result={}", response.getStatusCode(), response.getBody());
			return response.getStatusCode();
		}
		catch(HttpClientErrorException e) {
			LOGGER.error("Call to {} service failed with response code {} and message: {}", flavour.getActivityString(), e.getStatusCode(), e.getResponseBodyAsString());
			return e.getStatusCode();
		}
		catch(RestClientException e) {
			LOGGER.error("Call to {} service failed with message: {}", flavour.getActivityString(), e.getMessage());
			return null;
		}
	}

	/**
	 * Entry point to the de-registration of this node with the controller.
	 * @throws URISyntaxException 	If one of the generated URIs is a correct URI.
	 * @throws UnknownHostException	If the local host name of this machine could not be resolved into an address.
	 */
	protected void unregisterWithController() throws URISyntaxException, UnknownHostException {
		LOGGER.info("Starting to unregister with controller");

		WorkerNode node = configureWorkerNode();
		URI uri = new URI(createServiceUri(RegistrationFlavour.UNREGISTER) + "/" + node.getId());

		unregister(node, uri);
	}

	/**
	 * Tries to unregister the given WorkerNode at the given controller URI. As this will only be called while the application is exiting, there will be no
	 * retries in case of failure and there will be no return values.
	 */
	private void unregister(WorkerNode node, URI uri) {
		HttpHeaders headers = new HttpHeaders();
		HttpEntity<WorkerNode> entity = new HttpEntity<>(node, headers);

		final HttpStatus result = callRegistrationService(RegistrationFlavour.UNREGISTER, uri, entity);
		if(HttpStatus.OK == result) {
			LOGGER.info("Unregistered successfully @ {}", uri);
		}
		else {
			LOGGER.info("Unregistering not successful (response code {})", result);
		}
	}

	/**
	 * Waits for an amount of time specified in the config file and then returns.
	 */
	private void waitForRetry() {
		try {
			long delay = configProperties.getController().getRetryDelay();
			LOGGER.info("Waiting for {} ms before retrying", delay);
			Thread.sleep(delay);
		}
		catch(InterruptedException e) {
			LOGGER.error(e.getMessage(), e);
		}
	}

}
