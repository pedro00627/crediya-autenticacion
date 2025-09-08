package co.com.pragma.usecase.validation;

public final class ValidationConstants {

    // Salary validation constants
    public static final double MIN_BASE_SALARY = 0.0;
    public static final double MAX_BASE_SALARY = 15_000_000.0;
    // Error messages
    public static final String SALARY_OUT_OF_RANGE_MESSAGE = "El salario base debe estar entre 0 y 15,000,000.";
    public static final String ROLE_NOT_FOUND_MESSAGE = "El rol con ID '%d' no existe.";
    public static final String EMAIL_ALREADY_EXISTS_MESSAGE = "El correo electrónico '%s' ya se encuentra registrado.";

    private ValidationConstants() {
        // Private constructor to prevent instantiation
    }

}