package no.siriuslabs.computationapi.demo;

import no.siriuslabs.computationapi.implementation.config.ConfigProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class DemoControllerTest {

	@Mock
	private ConfigProperties configProperties;

	@InjectMocks
	private DemoController controller;

	@DisplayName("Test validation of the demo implementation")
	@Test
	public void testValidate() {
		// key == null
		assertEquals("", controller.validate(null, null));

		// another key - not hit
		assertEquals("", controller.validate("someOtherKey", null));

		// correct key + null value --> message
		assertEquals(DemoController.VALID_PROPERTY_MESSAGE, controller.validate(DemoController.VALID_KEY, null));

		// correct key + true value
		assertEquals("", controller.validate(DemoController.VALID_KEY, "true"));

		// correct key + false value --> message
		assertEquals(DemoController.VALID_PROPERTY_MESSAGE, controller.validate(DemoController.VALID_KEY, "false"));
	}

}
