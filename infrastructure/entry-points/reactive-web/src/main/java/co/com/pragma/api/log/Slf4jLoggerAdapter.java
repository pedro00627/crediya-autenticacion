package co.com.pragma.api.log;

import co.com.pragma.commonutils.LogHelper;
import co.com.pragma.model.log.gateways.LoggerPort;

public class Slf4jLoggerAdapter implements LoggerPort {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Slf4jLoggerAdapter.class);

    @Override
    public void info(String message, Object... args) {
        log.info(message, args);
    }

    @Override
    public void debug(String message, Object... args) {
        log.debug(message, args);
    }

    @Override
    public void error(String message, Throwable throwable) {
        log.error(message, throwable);
    }

    @Override
    public String maskEmail(String email) {
        return LogHelper.maskEmail(email);
    }

    @Override
    public String maskDocument(String documentId) {
        return LogHelper.maskDocument(documentId);
    }
}