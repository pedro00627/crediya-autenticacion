package co.com.pragma.model.log.gateways;

public interface LoggerPort {
    void info(String message, Object... args);

    void warn(String message, Object... args);

    void debug(String message, Object... args);

    void error(String message, Throwable throwable);

    String maskEmail(String email);

    String maskDocument(String documentId);
}
