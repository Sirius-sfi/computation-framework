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

public abstract class AbstractImplementationApplication {

	protected enum RegistrationFlavour {
		REGISTER("registering", "registerNode"),
		UNREGISTER("unregistering", "unregisterNode");

		private final String activityString;
		private final String serviceSubPath;

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

	private final ConfigProperties configProperties;

	private final RestTemplate restTemplate;

	@Value("${server.port}")
	private String serverPort;

	protected AbstractImplementationApplication(ConfigProperties configProperties) {
		this.configProperties = configProperties;
		this.restTemplate = new RestTemplate();
	}

	/**
	 * For testing only!
	 */
	protected AbstractImplementationApplication(ConfigProperties configProperties, RestTemplate restTemplate) {
		this.configProperties = configProperties;
		this.restTemplate = restTemplate;
	}

	protected boolean registerWithController() throws URISyntaxException, UnknownHostException {
		LOGGER.info("Starting to register with controller");

		WorkerNode node = configureWorkerNode();
		URI uri = createServiceUri(RegistrationFlavour.REGISTER);

		return register(node, uri);
	}

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

	private boolean isDockerActive() {
		String dockerFlag = System.getProperties().getProperty("docker");
		return dockerFlag == null || dockerFlag.trim().isEmpty() ? false : Boolean.parseBoolean(dockerFlag);
	}

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

	protected void unregisterWithController() throws URISyntaxException, UnknownHostException {
		LOGGER.info("Starting to unregister with controller");

		WorkerNode node = configureWorkerNode();
		URI uri = new URI(createServiceUri(RegistrationFlavour.UNREGISTER) + "/" + node.getId());

		unregister(node, uri);
	}

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
