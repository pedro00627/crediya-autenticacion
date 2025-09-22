package co.com.pragma.commonutils;

import co.com.pragma.commons.logging.LogHelper;
import co.com.pragma.model.log.gateways.LoggerPort;
import org.springframework.stereotype.Component;

@Component
public class LogHelperAdapter implements LoggerPort {

    @Override
    public void info(final String message, final Object... args) {
        LogHelper.info(message, args);
    }

    @Override
    public void warn(final String message, final Object... args) {
        LogHelper.warn(message, args);
    }

    @Override
    public void debug(final String message, final Object... args) {
        LogHelper.debug(message, args);
    }

    @Override
    public void error(final String message, final Throwable throwable) {
        LogHelper.error(message, throwable);
    }

    @Override
    public String maskEmail(final String email) {
        return LogHelper.maskEmail(email);
    }

    @Override
    public String maskDocument(final String documentId) {
        return LogHelper.maskDocument(documentId);
    }
}