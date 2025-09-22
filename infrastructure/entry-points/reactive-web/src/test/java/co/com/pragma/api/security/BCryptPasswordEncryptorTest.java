package co.com.pragma.api.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BCryptPasswordEncryptorTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    private BCryptPasswordEncryptor passwordEncryptor;

    @BeforeEach
    void setUp() {
        this.passwordEncryptor = new BCryptPasswordEncryptor(this.passwordEncoder);
    }

    @Test
    void shouldEncodePassword() {
        // Arrange
        final String rawPassword = "testPassword123";
        final String encodedPassword = "$2a$10$hashedPassword";
        when(this.passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        // Act
        final String result = this.passwordEncryptor.encode(rawPassword);

        // Assert
        assertEquals(encodedPassword, result);
        verify(this.passwordEncoder).encode(rawPassword);
    }

    @Test
    void shouldMatchValidPassword() {
        // Arrange
        final String rawPassword = "testPassword123";
        final String encodedPassword = "$2a$10$hashedPassword";
        when(this.passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        // Act
        final boolean result = this.passwordEncryptor.matches(rawPassword, encodedPassword);

        // Assert
        assertTrue(result);
        verify(this.passwordEncoder).matches(rawPassword, encodedPassword);
    }

    @Test
    void shouldNotMatchInvalidPassword() {
        // Arrange
        final String rawPassword = "wrongPassword";
        final String encodedPassword = "$2a$10$hashedPassword";
        when(this.passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        // Act
        final boolean result = this.passwordEncryptor.matches(rawPassword, encodedPassword);

        // Assert
        assertFalse(result);
        verify(this.passwordEncoder).matches(rawPassword, encodedPassword);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "password123",
            "complexPassword!@#",
            "simple",
            "veryLongPasswordWithManyCharacters123456789",
            "P@$$w0rd!",
            "123456",
            "",
            " ",
            "   spaces   "
    })
    void shouldEncodeVariousPasswords(final String rawPassword) {
        // Arrange
        final String expectedEncoded = "$2a$10$encoded_" + rawPassword.hashCode();
        when(this.passwordEncoder.encode(rawPassword)).thenReturn(expectedEncoded);

        // Act
        final String result = this.passwordEncryptor.encode(rawPassword);

        // Assert
        assertNotNull(result);
        assertEquals(expectedEncoded, result);
        verify(this.passwordEncoder).encode(rawPassword);
    }

    @ParameterizedTest
    @MethodSource("passwordMatchingTestCases")
    void shouldValidatePasswordMatching(final String rawPassword, final String encodedPassword,
                                        final boolean shouldMatch, final String scenario) {
        // Arrange
        when(this.passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(shouldMatch);

        // Act
        final boolean result = this.passwordEncryptor.matches(rawPassword, encodedPassword);

        // Assert
        assertEquals(shouldMatch, result, scenario);
        verify(this.passwordEncoder).matches(rawPassword, encodedPassword);
    }

    @ParameterizedTest
    @MethodSource("edgeCaseTestCases")
    void shouldHandleEdgeCases(final String rawPassword, final String encodedPassword,
                               final boolean expectedMatch, final String scenario) {
        // Arrange
        when(this.passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(expectedMatch);

        // Act
        final boolean result = this.passwordEncryptor.matches(rawPassword, encodedPassword);

        // Assert
        assertEquals(expectedMatch, result, scenario);
    }

    @Test
    void shouldDelegateToPasswordEncoder() {
        // Arrange
        final String rawPassword = "testPassword";
        final String encodedPassword = "$2a$10$hashedPassword";

        // Test encode
        when(this.passwordEncoder.encode(anyString())).thenReturn(encodedPassword);
        this.passwordEncryptor.encode(rawPassword);
        verify(this.passwordEncoder).encode(rawPassword);

        // Test matches
        when(this.passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        this.passwordEncryptor.matches(rawPassword, encodedPassword);
        verify(this.passwordEncoder).matches(rawPassword, encodedPassword);
    }

    static Stream<Arguments> passwordMatchingTestCases() {
        return Stream.of(
                Arguments.of("password123", "$2a$10$hashedPassword123", true, "Correct password should match"),
                Arguments.of("password123", "$2a$10$hashedPassword456", false, "Wrong password should not match"),
                Arguments.of("admin", "$2a$10$adminHash", true, "Admin password should match"),
                Arguments.of("guest", "$2a$10$adminHash", false, "Different password should not match"),
                Arguments.of("", "$2a$10$emptyHash", true, "Empty password should match empty hash"),
                Arguments.of("", "$2a$10$nonEmptyHash", false, "Empty password should not match non-empty hash"),
                Arguments.of("complexP@$$w0rd!", "$2a$10$complexHash", true, "Complex password should match"),
                Arguments.of("simple", "$2a$10$complexHash", false, "Simple password should not match complex hash")
        );
    }

    static Stream<Arguments> edgeCaseTestCases() {
        return Stream.of(
                Arguments.of(null, "$2a$10$hash", false, "Null raw password should not match"),
                Arguments.of("password", null, false, "Null encoded password should not match"),
                Arguments.of(null, null, false, "Both null passwords should not match"),
                Arguments.of("   ", "$2a$10$spaceHash", true, "Whitespace password should match if encoded correctly"),
                Arguments.of("password", "", false, "Password should not match empty encoded string"),
                Arguments.of("", "", true, "Both empty strings should match"),
                Arguments.of("unicode🔒", "$2a$10$unicodeHash", true, "Unicode password should match"),
                Arguments.of("case", "$2a$10$caseHash", false, "Case sensitive passwords should not match")
        );
    }
}