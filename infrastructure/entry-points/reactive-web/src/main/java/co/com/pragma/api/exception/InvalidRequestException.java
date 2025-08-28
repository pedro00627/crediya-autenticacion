package co.com.pragma.api.exception;

import jakarta.validation.ConstraintViolation;

import java.util.Set;

public class InvalidRequestException extends RuntimeException {
    private final Set<? extends ConstraintViolation> violations;

    public InvalidRequestException(Set<? extends ConstraintViolation> violations) {
        super("Invalid request due to validation errors.");
        this.violations = violations;
    }

    public Set<? extends ConstraintViolation> getViolations() {
        return violations;
    }
}