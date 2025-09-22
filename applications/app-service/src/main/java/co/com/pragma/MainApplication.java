package co.com.pragma;

import co.com.pragma.security.api.config.CommonSecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication
@ComponentScan(excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = CommonSecurityConfig.class))
public class MainApplication {
    public static void main(final String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
}
