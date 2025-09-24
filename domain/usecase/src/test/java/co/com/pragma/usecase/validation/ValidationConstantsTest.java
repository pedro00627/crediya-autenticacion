package co.com.pragma.usecase.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationConstantsTest {

    @Test
    void shouldHaveNoEnumValues() {
        assertEquals(0, ValidationConstants.values().length);
    }

    @Test
    void shouldHaveCorrectMinBaseSalary() {
        assertEquals(0.0, ValidationConstants.MIN_BASE_SALARY);
    }

    @Test
    void shouldHaveCorrectMaxBaseSalary() {
        assertEquals(15_000_000.0, ValidationConstants.MAX_BASE_SALARY);
    }

    @Test
    void shouldHaveValidSalaryRange() {
        assertTrue(ValidationConstants.MIN_BASE_SALARY < ValidationConstants.MAX_BASE_SALARY);
    }

    @Test
    void shouldHaveSalaryOutOfRangeMessage() {
        String expectedMessage = "El salario base debe estar entre 0 y 15,000,000.";
        assertEquals(expectedMessage, ValidationConstants.SALARY_OUT_OF_RANGE_MESSAGE);
    }

    @Test
    void shouldHaveRoleNotFoundMessage() {
        String expectedMessage = "El rol con ID '%d' no existe.";
        assertEquals(expectedMessage, ValidationConstants.ROLE_NOT_FOUND_MESSAGE);
    }

    @Test
    void shouldHaveEmailAlreadyExistsMessage() {
        String expectedMessage = "El correo electrónico '%s' ya se encuentra registrado.";
        assertEquals(expectedMessage, ValidationConstants.EMAIL_ALREADY_EXISTS_MESSAGE);
    }
}