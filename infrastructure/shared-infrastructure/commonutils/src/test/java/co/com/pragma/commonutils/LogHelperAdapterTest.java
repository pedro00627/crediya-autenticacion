package co.com.pragma.commonutils;

import co.com.pragma.model.log.gateways.LoggerPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LogHelperAdapterTest {

    private LogHelperAdapter logHelperAdapter;

    @BeforeEach
    void setUp() {
        logHelperAdapter = new LogHelperAdapter();
    }

    @Test
    void shouldImplementLoggerPort() {
        assertInstanceOf(LoggerPort.class, logHelperAdapter);
    }

    @Test
    void shouldExecuteInfoMethod() {
        assertDoesNotThrow(() -> logHelperAdapter.info("Test message"));
        assertDoesNotThrow(() -> logHelperAdapter.info("Test message with arg: {}", "value"));
    }

    @Test
    void shouldExecuteWarnMethod() {
        assertDoesNotThrow(() -> logHelperAdapter.warn("Test message"));
        assertDoesNotThrow(() -> logHelperAdapter.warn("Test message with arg: {}", "value"));
    }

    @Test
    void shouldExecuteDebugMethod() {
        assertDoesNotThrow(() -> logHelperAdapter.debug("Test message"));
        assertDoesNotThrow(() -> logHelperAdapter.debug("Test message with arg: {}", "value"));
    }

    @Test
    void shouldExecuteErrorMethod() {
        assertDoesNotThrow(() -> logHelperAdapter.error("Test message", new RuntimeException()));
    }

    @Test
    void shouldExecuteMaskEmailMethod() {
        String result = logHelperAdapter.maskEmail("test@example.com");
        assertNotNull(result);
    }

    @Test
    void shouldExecuteMaskDocumentMethod() {
        String result = logHelperAdapter.maskDocument("12345678");
        assertNotNull(result);
    }
}