package co.com.pragma.api.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PasswordEncoderConfigTest {

    @Test
    void shouldReturnBCryptPasswordEncoder() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(PasswordEncoderConfig.class);
        PasswordEncoder passwordEncoder = context.getBean(PasswordEncoder.class);

        assertNotNull(passwordEncoder);
        assertInstanceOf(BCryptPasswordEncoder.class, passwordEncoder);

        context.close();
    }
}