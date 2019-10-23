package no.siriuslabs.computationapi.controller;

import no.siriuslabs.computationapi.api.model.computation.DomainType;
import no.siriuslabs.computationapi.api.model.config.Controller;
import no.siriuslabs.computationapi.api.model.request.Payload;
import no.siriuslabs.computationapi.config.ControllerProperties;
import no.siriuslabs.computationapi.service.NodeRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@ActiveProfiles("test")
public class ServiceControllerTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceControllerTest.class);

	private static final String SUCCESS_NODE_ID = "successNode";
	private static final String RESP_CODE_FAIL_NODE_ID = "responseCodeFailNode";
	private static final String NULL_NODE_ID = "nullNode";
	private static final String FAIL_NODE_ID = "failNode";

	@Mock
	private NodeRegistry nodeRegistry;

	@Mock
	private RestTemplate restTemplate;

	@Mock
	private ControllerProperties controllerProperties;

	@InjectMocks
	private ServiceController serviceController;

	@DisplayName("Test reserveNode() without successfully reserving a node")
	@Test
	public void testReserveNode_NoResult() {
		Controller controller = new Controller();
		final int retryCount = 3;
		controller.setRetryCount(retryCount);
		controller.setRetryDelay(10);
		Mockito.when(controllerProperties.getController()).thenReturn(controller);

		String resultNodeId = serviceController.reserveNode(DomainType.DEMO);

		assertNull(resultNodeId, "Node ID expected to be null after failed reservation of node");
		Mockito.verify(nodeRegistry, Mockito.times(retryCount)).reserveNode(DomainType.DEMO);
	}

	@DisplayName("Test reserveNode() with successfully reserving a node")
	@Test
	public void testReserveNode_Success() {
		Controller controller = new Controller();
		controller.setRetryCount(3);
		controller.setRetryDelay(10);
		Mockito.when(controllerProperties.getController()).thenReturn(controller);

		final String reservedNodeID = "reservedNodeID";
		Mockito.when(nodeRegistry.reserveNode(DomainType.DEMO)).thenReturn(reservedNodeID);

		String resultNodeId = serviceController.reserveNode(DomainType.DEMO);

		assertEquals(reservedNodeID, resultNodeId, "Node ID expected to be " + reservedNodeID + " after successful reservation");
		Mockito.verify(nodeRegistry, Mockito.times(1)).reserveNode(DomainType.DEMO);
	}

	@DisplayName("Test validateData() with successful validation result")
	@Test
	public void testValidateData_Success() {
		try {
			final URI successNodeUri = new URI("http://sirius-labs.no/" + SUCCESS_NODE_ID);

			Mockito.when(nodeRegistry.getUriForNode(SUCCESS_NODE_ID)).thenReturn(successNodeUri);

			// try successful validation
			ResponseEntity<Object> successResponse = ResponseEntity.status(HttpStatus.OK).body(new ArrayList<String>());
			Mockito.when(restTemplate.exchange(Mockito.eq(new URI(successNodeUri + ServiceController.VALIDATE_DATA_PATH)), Mockito.eq(HttpMethod.POST), Mockito.any(HttpEntity.class), Mockito.eq(Object.class))).thenReturn(successResponse);

			assertNull(serviceController.validateData(SUCCESS_NODE_ID, new Payload()), "Validation of correct data must be accepted");
		}
		catch(URISyntaxException e) {
			fail(e.getMessage());
			LOGGER.error(e.getMessage(), e);
		}
	}

	@DisplayName("Test validateData() with fail because of response code")
	@Test
	public void testValidateData_ResponseCode() {
		try {
			final URI responseCodeFailNodeUri = new URI("http://sirius-labs.no/" + RESP_CODE_FAIL_NODE_ID);

			Mockito.when(nodeRegistry.getUriForNode(RESP_CODE_FAIL_NODE_ID)).thenReturn(responseCodeFailNodeUri);

			// try validation with response code != 200 (OK)
			ResponseEntity<Object> responseCodeFailResponse = ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body(new ArrayList<String>());
			Mockito.when(restTemplate.exchange(Mockito.eq(new URI(responseCodeFailNodeUri + ServiceController.VALIDATE_DATA_PATH)), Mockito.eq(HttpMethod.POST), Mockito.any(HttpEntity.class), Mockito.eq(Object.class))).thenReturn(responseCodeFailResponse);

			ResponseEntity<Object> result = serviceController.validateData(RESP_CODE_FAIL_NODE_ID, new Payload());
			assertEquals(HttpStatus.NOT_ACCEPTABLE, result.getStatusCode(), "Validation with response code != 200 must be rejected");
			assertEquals(ServiceController.DATA_VALIDATION_FAILED_RESPONSE_CODE_MSG + responseCodeFailResponse.getStatusCode(), result.getBody(), "Validation with response code != 200 must be rejected");
		}
		catch(URISyntaxException e) {
			fail(e.getMessage());
			LOGGER.error(e.getMessage(), e);
		}
	}

	@DisplayName("Test validateData() with fail because of null response body")
	@Test
	public void testValidateData_NullBody() {
		try {
			final URI nullNodeUri = new URI("http://sirius-labs.no/" + NULL_NODE_ID);

			Mockito.when(nodeRegistry.getUriForNode(NULL_NODE_ID)).thenReturn(nullNodeUri);

			// try validation with null result in the body
			ResponseEntity<Object> nullResponse = new ResponseEntity<>(HttpStatus.OK);
			Mockito.when(restTemplate.exchange(Mockito.eq(new URI(nullNodeUri + ServiceController.VALIDATE_DATA_PATH)), Mockito.eq(HttpMethod.POST), Mockito.any(HttpEntity.class), Mockito.eq(Object.class))).thenReturn(nullResponse);

			ResponseEntity<Object> result = serviceController.validateData(NULL_NODE_ID, new Payload());
			assertEquals(HttpStatus.NOT_ACCEPTABLE, result.getStatusCode(), "Validation with response code 200 but null body must be rejected (likely error)");
			assertEquals(ServiceController.DATA_VALIDATION_FAILED_NO_RESULT_MSG, result.getBody(), "Validation with response code 200 but null body must be rejected (likely error)");
		}
		catch(URISyntaxException e) {
			fail(e.getMessage());
			LOGGER.error(e.getMessage(), e);
		}
	}

	@DisplayName("Test validateData() with fail because of error messages in body")
	@Test
	public void testValidateData_ErrorMessages() {
		try {
			final URI failNodeUri = new URI("http://sirius-labs.no/" + FAIL_NODE_ID);

			Mockito.when(nodeRegistry.getUriForNode(FAIL_NODE_ID)).thenReturn(failNodeUri);

			// try validation with validation error messages in the body
			final List<String> messages = Arrays.asList("error message 1", "error message 2");
			ResponseEntity<Object> failResponse = ResponseEntity.ok(messages);
			Mockito.when(restTemplate.exchange(Mockito.eq(new URI(failNodeUri + ServiceController.VALIDATE_DATA_PATH)), Mockito.eq(HttpMethod.POST), Mockito.any(HttpEntity.class), Mockito.eq(Object.class))).thenReturn(failResponse);

			ResponseEntity<Object> result = serviceController.validateData(FAIL_NODE_ID, new Payload());
			assertEquals(HttpStatus.NOT_ACCEPTABLE, result.getStatusCode(), "Validation with response code 200 but errors messages must be rejected");
			assertEquals(ServiceController.DATA_VALIDATION_FAILED_MSG + messages, result.getBody(), "Validation with response code 200 but errors messages must be rejected");
		}
		catch(URISyntaxException e) {
			fail(e.getMessage());
			LOGGER.error(e.getMessage(), e);
		}
	}

}
