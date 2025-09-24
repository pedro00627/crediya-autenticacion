package co.com.pragma;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class MainApplicationTest {

    @Test
    void contextLoads() {
    }

    @Test
    void shouldStartMainApplication() {
        assertDoesNotThrow(() -> MainApplication.main(new String[]{}));
    }
}