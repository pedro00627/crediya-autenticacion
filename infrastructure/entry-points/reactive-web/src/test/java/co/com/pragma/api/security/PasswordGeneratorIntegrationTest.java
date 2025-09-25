package co.com.pragma.api.security;

import co.com.pragma.security.impl.BCryptPasswordEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test de integración para generar contraseñas cifradas reales para la base de datos.
 * Este test NO usa mocks - genera hashes BCrypt reales que puedes usar directamente.
 */
class PasswordGeneratorIntegrationTest {

    private BCryptPasswordEncryptor passwordEncryptor;
    private PasswordEncoder realPasswordEncoder;

    @BeforeEach
    void setUp() {
        // Usar el encoder real, no mocks
        realPasswordEncoder = new BCryptPasswordEncoder();
        passwordEncryptor = new BCryptPasswordEncryptor(realPasswordEncoder);
    }

    @Test
    void shouldGenerateRealHashForDefaultPassword() {
        // Contraseña por defecto para testing
        final String rawPassword = "password123";

        // Generar hash real
        String encodedPassword = passwordEncryptor.encode(rawPassword);

        // Mostrar en consola para copiar a la base de datos
        System.out.println("=".repeat(80));
        System.out.println("CONTRASENA CIFRADA PARA BASE DE DATOS:");
        System.out.println("Contrasena sin cifrar: " + rawPassword);
        System.out.println("Contrasena cifrada: " + encodedPassword);
        System.out.println("=".repeat(80));

        // Verificar que funciona
        assertTrue(passwordEncryptor.matches(rawPassword, encodedPassword));
    }

    @ParameterizedTest
    @CsvSource({
            "admin123, 'Contrasena para usuario admin'",
            "client123, 'Contrasena para cliente de prueba'",
            "advisor123, 'Contrasena para asesor de prueba'",
            "test123, 'Contrasena generica de testing'"
    })
    void shouldGeneratePasswordsForDifferentUsers(String rawPassword, String description) {
        // Generar hash real
        String encodedPassword = passwordEncryptor.encode(rawPassword);

        // Mostrar en consola
        System.out.println("\n" + "=".repeat(60));
        System.out.println(description);
        System.out.println("Contrasena: " + rawPassword);
        System.out.println("Hash BCrypt: " + encodedPassword);
        System.out.println("=".repeat(60));

        // Verificar que funciona
        assertTrue(passwordEncryptor.matches(rawPassword, encodedPassword));
    }

    @Test
    void shouldGenerateHashForCommonCredentials() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("CREDENCIALES COMUNES PARA PRUEBAS:");
        System.out.println("=".repeat(80));

        // Usuario admin
        final String adminPassword = "admin123";
        final String adminHash = passwordEncryptor.encode(adminPassword);
        System.out.println("ADMIN - Email: admin@crediya.com");
        System.out.println("ADMIN - Password: " + adminPassword);
        System.out.println("ADMIN - Hash: " + adminHash);
        System.out.println();

        // Usuario cliente
        final String clientPassword = "client123";
        final String clientHash = passwordEncryptor.encode(clientPassword);
        System.out.println("CLIENTE - Email: cliente@test.com");
        System.out.println("CLIENTE - Password: " + clientPassword);
        System.out.println("CLIENTE - Hash: " + clientHash);
        System.out.println();

        // Usuario asesor
        final String advisorPassword = "advisor123";
        final String advisorHash = passwordEncryptor.encode(advisorPassword);
        System.out.println("ASESOR - Email: asesor@crediya.com");
        System.out.println("ASESOR - Password: " + advisorPassword);
        System.out.println("ASESOR - Hash: " + advisorHash);
        System.out.println("=".repeat(80));

        // Verificar que todos funcionan
        assertTrue(passwordEncryptor.matches(adminPassword, adminHash));
        assertTrue(passwordEncryptor.matches(clientPassword, clientHash));
        assertTrue(passwordEncryptor.matches(advisorPassword, advisorHash));
    }

    @Test
    void shouldGenerateSpecificPasswordForCurrentIssue() {
        // Si tienes una contraseña específica que necesitas cifrar, ponla aquí
        final String specificPassword = "password123"; // Cambia esto por la contraseña que necesites

        String encodedPassword = passwordEncryptor.encode(specificPassword);

        System.out.println("\n" + "=".repeat(80));
        System.out.println("CONTRASENA ESPECIFICA PARA ACTUALIZAR EN BD:");
        System.out.println("Contrasena original: " + specificPassword);
        System.out.println("Hash para la BD: " + encodedPassword);
        System.out.println();
        System.out.println("SQL UPDATE ejemplo:");
        System.out.println("UPDATE users SET password = '" + encodedPassword + "' WHERE email = 'tu-email@test.com';");
        System.out.println("=".repeat(80));

        // Verificar
        assertTrue(passwordEncryptor.matches(specificPassword, encodedPassword));
    }
}