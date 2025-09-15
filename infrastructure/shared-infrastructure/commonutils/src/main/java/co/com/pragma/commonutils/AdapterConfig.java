package co.com.pragma.commonutils;

import co.com.pragma.model.log.gateways.LoggerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdapterConfig {

    @Bean
    public LoggerPort loggerPort() {
        return new LogHelperAdapter();
    }
}