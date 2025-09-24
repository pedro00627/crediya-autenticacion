package co.com.pragma.model.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BusinessExceptionTest {
    @Test
    void BusinessExceptionTestHasException() {
        final String err = "Has error";
        final BusinessException exception = new BusinessException(err);
        Assertions.assertEquals(err, exception.getMessage());
    }
}