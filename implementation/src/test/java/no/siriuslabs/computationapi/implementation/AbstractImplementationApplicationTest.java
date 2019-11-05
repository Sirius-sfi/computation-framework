package no.siriuslabs.computationapi.implementation;

import no.siriuslabs.computationapi.api.model.computation.DomainType;
import no.siriuslabs.computationapi.api.model.config.Controller;
import no.siriuslabs.computationapi.api.model.node.WorkerNode;
import no.siriuslabs.computationapi.implementation.config.ConfigProperties;
import no.siriuslabs.computationapi.implementation.config.Node;
import no.siriuslabs.computationapi.implementation.model.TestDomainType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
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

import static org.junit.jupiter.api.Assertions.*;

public class AbstractImplementationApplicationTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractImplementationApplicationTest.class);

	private ConfigProperties configProperties;
	private RestTemplate restTemplate;

	private AbstractImplementationApplication application;

	@BeforeEach
	public void setup() {
		configProperties = Mockito.mock(ConfigProperties.class);
		restTemplate = Mockito.mock(RestTemplate.class);
		application = new AbstractImplementationApplication(configProperties, restTemplate) {};
	}

	@DisplayName("Test worker node configuration")
	@Test
	public void testConfigureWorkerNode() throws UnknownHostException {
		Node node = new Node();
		node.setDomain(TestDomainType.TEST_1);

		Mockito.when(configProperties.getNode()).thenReturn(node);

		// as we do not have the Spring environment available fo the test, the server port used in configureWorkerNode() will be null - we ignore this for now
		final WorkerNode[] result = new WorkerNode[1];
		assertAll(() -> { result[0] = application.configureWorkerNode(); });

		assertNotNull(result[0], "configureWorkerNode() is expected to return a non-null value");
		assertTrue(result[0].getId().contains(InetAddress.getLocalHost().getHostAddress()) || result[0].getId().contains(InetAddress.getLocalHost().getHostName()), "Node's ID is expected to contain host's IP address or host name");
		assertEquals(TestDomainType.TEST_1, result[0].getDomainType(), "Node's domain type must be as configured above");
	}

	@DisplayName("Test creating the controller's service URI ")
	@Test
	public void testCreateServiceUri() {
		final String urlString = "http://sirius-labs.no/";

		Controller controller = new Controller();
		try {
			controller.setLocalUrl(new URI(urlString));

			Mockito.when(configProperties.getController()).thenReturn(controller);

			URI result = application.createServiceUri(AbstractImplementationApplication.RegistrationFlavour.REGISTER);

			assertEquals(urlString + AbstractImplementationApplication.RegistrationFlavour.REGISTER.getServiceSubPath(), result.toString(), "URI is expected to match the URI set above");
		}
		catch(URISyntaxException e) {
			Assertions.fail(e.getMessage());
			LOGGER.error(e.getMessage(), e);
		}
	}

	@DisplayName("Test register() without successfully registering with the controller")
	@Test
	public void testRegister_NoSuccess() {
		Controller controller = new Controller();
		controller.setRetryCount(3);
		controller.setRetryDelay(10);
		Mockito.when(configProperties.getController()).thenReturn(controller);

		URI uri = createUri();

		Mockito.when(restTemplate.exchange(Mockito.eq(uri), Mockito.eq(HttpMethod.POST), Mockito.any(HttpEntity.class), Mockito.eq(Object.class))).thenReturn(new ResponseEntity<Object>(HttpStatus.I_AM_A_TEAPOT));

		assertFalse(application.register(new WorkerNode(), uri), "Failure to register with the controller is expected to produce false");
	}

	@DisplayName("Test register() and successfully register with the controller")
	@Test
	public void testRegister_Success() {
		Controller controller = new Controller();
		controller.setRetryCount(3);
		controller.setRetryDelay(10);
		Mockito.when(configProperties.getController()).thenReturn(controller);

		URI uri = createUri();

		Mockito.when(restTemplate.exchange(Mockito.eq(uri), Mockito.eq(HttpMethod.POST), Mockito.any(HttpEntity.class), Mockito.eq(Object.class))).thenReturn(new ResponseEntity<Object>(HttpStatus.OK));

		assertTrue(application.register(new WorkerNode(), uri), "Successful registration with the controller is expected to produce true");
	}

	@DisplayName("Test callRegisterService() with RestClientException thrown by the call")
	@Test
	public void testCallRegisterService_RestExc() {
		final URI uri = createUri();
		Mockito.when(restTemplate.exchange(Mockito.eq(uri), Mockito.eq(HttpMethod.POST), Mockito.any(HttpEntity.class), Mockito.eq(Object.class))).thenThrow(new RestClientException("Expected RestClientException"));

		assertNull(application.callRegistrationService(AbstractImplementationApplication.RegistrationFlavour.REGISTER, uri, new HttpEntity<>(new WorkerNode())), "Null is expected to be returned after RestClientException in the call");
	}

	@DisplayName("Test callRegisterService() with HttpClientErrorException thrown by the call")
	@Test
	public void testCallRegisterService_HttpExc() {
		final URI uri = createUri();
		Mockito.when(restTemplate.exchange(Mockito.eq(uri), Mockito.eq(HttpMethod.POST), Mockito.any(HttpEntity.class), Mockito.eq(Object.class))).thenThrow(new HttpClientErrorException(HttpStatus.I_AM_A_TEAPOT));

		assertEquals(HttpStatus.I_AM_A_TEAPOT, application.callRegistrationService(AbstractImplementationApplication.RegistrationFlavour.REGISTER, uri, new HttpEntity<>(new WorkerNode())), "Exception's status code is expected to be returned after RestClientException in the call");
	}

	@DisplayName("Test callRegisterService() with success and status code 200 (OK)")
	@Test
	public void testCallRegisterService_Success() {
		final URI uri = createUri();
		Mockito.when(restTemplate.exchange(Mockito.eq(uri), Mockito.eq(HttpMethod.POST), Mockito.any(HttpEntity.class), Mockito.eq(Object.class))).thenReturn(new ResponseEntity<Object>(HttpStatus.OK));

		assertEquals(HttpStatus.OK, application.callRegistrationService(AbstractImplementationApplication.RegistrationFlavour.REGISTER, uri, new HttpEntity<>(new WorkerNode())), "Status code 200 (OK) is expected to be returned after successful call");
	}

	private URI createUri() {
		try {
			return new URI("http://sirius-labs.no");
		}
		catch(URISyntaxException e) {
			Assertions.fail(e.getMessage());
			LOGGER.error(e.getMessage(), e);
		}
		return null;
	}

}
