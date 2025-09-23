package co.com.pragma.commonutils;

import co.com.pragma.commons.logging.LogHelper;
import co.com.pragma.model.log.gateways.LoggerPort;
import org.springframework.stereotype.Component;

@Component
public class LogHelperAdapter implements LoggerPort {

    @Override
    public void info(String message, Object... args) {
        LogHelper.info(message, args);
    }

    @Override
    public void warn(String message, Object... args) {
        LogHelper.warn(message, args);
    }

    @Override
    public void debug(String message, Object... args) {
        LogHelper.debug(message, args);
    }

    @Override
    public void error(String message, Throwable throwable) {
        LogHelper.error(message, throwable);
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