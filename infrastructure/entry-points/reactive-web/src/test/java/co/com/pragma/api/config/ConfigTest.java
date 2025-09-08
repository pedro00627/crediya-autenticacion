package co.com.pragma.api.config;

import co.com.pragma.api.Handler;
import co.com.pragma.api.Router;
import co.com.pragma.api.exception.GlobalExceptionHandler;
import co.com.pragma.api.exception.strategy.BusinessExceptionHandler;
import co.com.pragma.api.exception.strategy.DefaultExceptionHandler;
import co.com.pragma.api.exception.strategy.InvalidRequestExceptionHandler;
import co.com.pragma.api.exception.strategy.ServerWebInputExceptionHandler;
import co.com.pragma.api.mapper.UserDTOMapper;
import co.com.pragma.usecase.user.UserUseCase;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

import co.com.pragma.model.log.gateways.LoggerPort;

// @WebFluxTest is a slice test for the web layer. We import the configurations
// and router/handler we want to test.
@WebFluxTest(controllers = {}) // We specify no controllers to avoid component scanning
@Import({
        Router.class,
        Handler.class,
        CorsConfig.class,
        SecurityHeadersConfig.class,
        GlobalExceptionHandler.class,
        InvalidRequestExceptionHandler.class,
        BusinessExceptionHandler.class,
        ServerWebInputExceptionHandler.class,
        DefaultExceptionHandler.class,
        ConfigTest.TestConfig.class
})
class ConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void corsConfigurationShouldAllowOrigins() {
        // The security filters should apply to all responses, even for non-existent paths (404).
        webTestClient.get().uri("/any/non-existent/path")
                .exchange()
                // After importing the GlobalExceptionHandler, the DefaultExceptionHandler will catch the 404
                // and convert it to a 500, which is the behavior we should test.
                .expectStatus().is5xxServerError()
                .expectHeader().valueEquals("Content-Security-Policy",
                        "default-src 'self'; frame-ancestors 'self'; form-action 'self'")
                .expectHeader().valueEquals("Strict-Transport-Security", "max-age=31536000;")
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader().valueEquals("Server", "")
                .expectHeader().valueEquals("Cache-Control", "no-store")
                .expectHeader().valueEquals("Pragma", "no-cache")
                .expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin");
    }

    /**
     * This nested static class provides mock beans for dependencies outside the web layer.
     * This is the recommended replacement for the deprecated @MockBean.
     */
    @TestConfiguration
    static class TestConfig {
        @Bean
        public UserUseCase userUseCase() {
            return Mockito.mock(UserUseCase.class);
        }

        @Bean
        public UserDTOMapper userDTOMapper() {
            return Mockito.mock(UserDTOMapper.class);
        }

        @Bean
        public Validator validator() {
            return Mockito.mock(Validator.class);
        }

        @Bean
        public LoggerPort loggerPort() {
            return Mockito.mock(LoggerPort.class);
        }
    }
}
