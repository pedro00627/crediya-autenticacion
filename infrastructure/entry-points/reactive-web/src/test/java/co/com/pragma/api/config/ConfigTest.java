package co.com.pragma.api.config;
/*
import co.com.pragma.api.Handler;
import co.com.pragma.api.Router;
import co.com.pragma.api.exception.GlobalExceptionHandler;
import co.com.pragma.api.exception.strategy.BusinessExceptionHandler;
import co.com.pragma.api.exception.strategy.DefaultExceptionHandler;
import co.com.pragma.api.exception.strategy.InvalidRequestExceptionHandler;
import co.com.pragma.api.exception.strategy.ServerWebInputExceptionHandler;
import co.com.pragma.api.mapper.UserDTOMapper;
import co.com.pragma.model.log.gateways.LoggerPort;
import co.com.pragma.usecase.user.UserUseCase;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.security.crypto.password.PasswordEncoder;

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
        DefaultExceptionHandler.class
})
class ConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private UserUseCase userUseCase;

    @MockitoBean
    private UserDTOMapper userDTOMapper;

    @MockitoBean
    private Validator validator;

    @MockitoBean
    private LoggerPort loggerPort;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

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
}
*/