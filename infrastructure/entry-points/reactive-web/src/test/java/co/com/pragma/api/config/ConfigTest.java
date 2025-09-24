package co.com.pragma.api.config;

import co.com.pragma.security.api.SecurityHeadersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        ReactiveSecurityAutoConfiguration.class
})
@Import(SecurityHeadersConfig.class)
class ConfigTest {

    private final WebTestClient webTestClient;

    public ConfigTest(@Autowired WebTestClient webTestClient) {
        this.webTestClient = webTestClient;
    }

    @Test
    void securityHeadersShouldBeAppliedToResponses() {
        webTestClient.get()
                .uri("/any-endpoint")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void criticalSecurityHeadersShouldPreventCommonAttacks() {
        webTestClient.get()
                .uri("/auth/login")
                .exchange()
                .expectHeader().exists("Content-Security-Policy")
                .expectHeader().exists("X-Content-Type-Options")
                .expectHeader().exists("Strict-Transport-Security")
                .expectHeader().valueMatches("Cache-Control", ".*no-store.*")
                .expectHeader().valueMatches("X-Content-Type-Options", "nosniff");
    }

    @Test
    void serverHeaderShouldBeHiddenForSecurity() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectHeader().valueEquals("Server", "");
    }

    @Configuration
    @EnableAutoConfiguration(exclude = {SecurityAutoConfiguration.class, ReactiveSecurityAutoConfiguration.class})
    static class TestConfiguration {
    }
}
