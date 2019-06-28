package no.siriuslabs.computationapi.controller;

import no.siriuslabs.computationapi.api.exception.InvalidParameterException;
import no.siriuslabs.computationapi.api.model.computation.DomainType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
public class ControllerHelperTest {

	@DisplayName("Test checkParameter() with null value")
	@Test
	public void testCheckParameter_NullValue() {
		// run with null parameter value
		Exception result = assertThrows(InvalidParameterException.class, () -> { ControllerHelper.checkParameter(null); }, "Null parameter value is expected to throw exception");
		assertEquals(ControllerHelper.PARAMETER_MUST_NOT_BE_EMPTY_TEXT, result.getMessage());
	}

	@DisplayName("Test checkParameter() with empty string value")
	@Test
	public void testCheckParameter_EmptyString() {
		// run with empty string as parameter value
		Exception result = assertThrows(InvalidParameterException.class, () -> { ControllerHelper.checkParameter(""); }, "Empty parameter value is expected to throw exception");
		assertEquals(ControllerHelper.PARAMETER_MUST_NOT_BE_EMPTY_TEXT, result.getMessage());
	}

	@DisplayName("Test checkParameter() with blanks value")
	@Test
	public void testCheckParameter_BlanksOnly() {
		// run with only blanks as parameter value
		Exception result = assertThrows(InvalidParameterException.class, () -> { ControllerHelper.checkParameter("    "); }, "Blanks-only parameter value is expected to throw exception");
		assertEquals(ControllerHelper.PARAMETER_MUST_NOT_BE_EMPTY_TEXT, result.getMessage());
	}

	@DisplayName("Test checkParameter() with valid value")
	@Test
	public void testCheckParameter_Valid() {
		// run with valid parameter value
		assertDoesNotThrow(() -> { ControllerHelper.checkParameter("thisIsValid"); }, "Valid parameter value must not throw exception");
	}

	@DisplayName("Test getDomainTypeFromParameter() with null value")
	@Test
	public void testGetDomainTypeFromParameter_NullValue() {
		// try with null value
		Exception thrown = assertThrows(InvalidParameterException.class, () -> { ControllerHelper.getDomainTypeFromParameter(null); }, "Null as parameter value is expected to throw exception");
		assertEquals(ControllerHelper.UNKNOWN_OR_UNSUPPORTED_DOMAIN_TEXT + null, thrown.getMessage());
	}

	@DisplayName("Test getDomainTypeFromParameter() with non-enum value")
	@Test
	public void testGetDomainTypeFromParameter_Unknown() {
		// try something not in the enum
		final String parameterValue = "not there";
		Exception thrown = assertThrows(InvalidParameterException.class, () -> { ControllerHelper.getDomainTypeFromParameter(parameterValue); }, "Parameter value not present in the enum is expected to throw exception");
		assertEquals(ControllerHelper.UNKNOWN_OR_UNSUPPORTED_DOMAIN_TEXT + parameterValue, thrown.getMessage());
	}

	@DisplayName("Test getDomainTypeFromParameter() with valid value")
	@Test
	public void testGetDomainTypeFromParameter_Valid() {
		// try a valid enum element
		DomainType result = ControllerHelper.getDomainTypeFromParameter(DomainType.DEMO.toString().toLowerCase());
		assertEquals(DomainType.DEMO, result);
	}

}
