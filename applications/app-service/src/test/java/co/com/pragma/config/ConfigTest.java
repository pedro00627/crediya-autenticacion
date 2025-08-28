package co.com.pragma.config;

import co.com.pragma.api.Handler;
import co.com.pragma.api.Router;
import co.com.pragma.usecase.user.UserUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

// Use @WebFluxTest to test the web layer in isolation.
// We explicitly import the Router and Handler to be tested.
@WebFluxTest
@Import({Router.class, Handler.class})
public class ConfigTest {

    @Autowired
    private WebTestClient webTestClient;
    // from a lower layer (like the use case) that the web layer needs.
    private UserUseCase userUseCase;

    @Test
    void corsConfigurationShouldAllowOrigins() {
        // This is an example test for a CORS configuration.
        // It verifies that requests from a specific origin are allowed.
        webTestClient.options().uri("/api/users") // Assuming a route exists
                .header("Origin", "http://example.com")
                .header("Access-Control-Request-Method", "POST")
                .exchange()
                .expectStatus().isOk();
    }
}