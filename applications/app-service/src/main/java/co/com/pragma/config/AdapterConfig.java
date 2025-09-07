package co.com.pragma.config;

import co.com.pragma.api.log.Slf4jLoggerAdapter;
import co.com.pragma.model.log.gateways.LoggerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdapterConfig {

    @Bean
    public LoggerPort loggerPort() {
        return new Slf4jLoggerAdapter();
    }
}