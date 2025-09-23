package co.com.pragma.model.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserTest {

    @Test
    void shouldCreateUserWithAllFields() {
        // Arrange & Act
        User user = new User(
                "1",
                "John",
                "Doe",
                LocalDate.of(1990, 5, 15),
                "john.doe@example.com",
                "123456789",
                "3001234567",
                1,
                50000.0,
                "password123"
        );

        // Assert
        assertNotNull(user);
        assertEquals("1", user.id());
        assertEquals("John", user.firstName());
        assertEquals("Doe", user.lastName());
        assertEquals(LocalDate.of(1990, 5, 15), user.birthDate());
        assertEquals("john.doe@example.com", user.email());
        assertEquals("123456789", user.identityDocument());
        assertEquals("3001234567", user.phone());
        assertEquals(1, user.roleId());
        assertEquals(50000.0, user.baseSalary());
        assertEquals("password123", user.password());
    }

    @Test
    void shouldCreateUserWithNullFields() {
        // Arrange & Act
        User user = new User(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        // Assert
        assertNotNull(user);
    }

    @ParameterizedTest
    @MethodSource("userEqualityTestCases")
    void shouldValidateUserEquality(User user1, User user2, boolean expectedEqual, String scenario) {
        // Act & Assert
        if (expectedEqual) {
            assertEquals(user1, user2, scenario);
            assertEquals(user1.hashCode(), user2.hashCode(), "Hash codes should be equal for equal users");
        } else {
            assertNotEquals(user1, user2, scenario);
        }
    }

    @ParameterizedTest
    @MethodSource("userCreationTestCases")
    void shouldCreateUserWithDifferentValues(String id, String firstName, String lastName,
                                             LocalDate birthDate, String email, String identityDocument,
                                             String phone, Integer roleId, Double baseSalary,
                                             String password, String scenario) {
        // Arrange & Act
        User user = new User(id, firstName, lastName, birthDate, email, identityDocument,
                           phone, roleId, baseSalary, password);

        // Assert
        assertNotNull(user, scenario);
        assertEquals(id, user.id());
        assertEquals(firstName, user.firstName());
        assertEquals(lastName, user.lastName());
        assertEquals(birthDate, user.birthDate());
        assertEquals(email, user.email());
        assertEquals(identityDocument, user.identityDocument());
        assertEquals(phone, user.phone());
        assertEquals(roleId, user.roleId());
        assertEquals(baseSalary, user.baseSalary());
        assertEquals(password, user.password());
    }

    static Stream<Arguments> userEqualityTestCases() {
        User baseUser = new User("1", "John", "Doe", LocalDate.of(1990, 5, 15),
                               "john@example.com", "123456789", "3001234567", 1, 50000.0, "pass");
        User identicalUser = new User("1", "John", "Doe", LocalDate.of(1990, 5, 15),
                                    "john@example.com", "123456789", "3001234567", 1, 50000.0, "pass");
        User differentId = new User("2", "John", "Doe", LocalDate.of(1990, 5, 15),
                                  "john@example.com", "123456789", "3001234567", 1, 50000.0, "pass");
        User differentFirstName = new User("1", "Jane", "Doe", LocalDate.of(1990, 5, 15),
                                         "john@example.com", "123456789", "3001234567", 1, 50000.0, "pass");
        User differentEmail = new User("1", "John", "Doe", LocalDate.of(1990, 5, 15),
                                     "jane@example.com", "123456789", "3001234567", 1, 50000.0, "pass");

        return Stream.of(
                Arguments.of(baseUser, identicalUser, true, "Identical users should be equal"),
                Arguments.of(baseUser, differentId, false, "Users with different IDs should not be equal"),
                Arguments.of(baseUser, differentFirstName, false, "Users with different first names should not be equal"),
                Arguments.of(baseUser, differentEmail, false, "Users with different emails should not be equal")
        );
    }

    static Stream<Arguments> userCreationTestCases() {
        return Stream.of(
                Arguments.of("1", "John", "Doe", LocalDate.of(1990, 5, 15), "john@example.com",
                           "123456789", "3001234567", 1, 50000.0, "pass", "Standard user creation"),
                Arguments.of(null, "John", "Doe", LocalDate.of(1990, 5, 15), "john@example.com",
                           "123456789", "3001234567", 1, 50000.0, "pass", "User creation with null ID"),
                Arguments.of("2", "Jane", "Smith", LocalDate.of(1985, 12, 25), "jane@example.com",
                           "987654321", "3109876543", 2, 75000.0, "secret", "Different user data"),
                Arguments.of("3", "Admin", "User", LocalDate.of(1980, 1, 1), "admin@example.com",
                           "111222333", "3201112222", 3, 100000.0, "admin123", "Admin user creation"),
                Arguments.of("4", "Test", "User", LocalDate.now(), "test@example.com",
                           "444555666", "3154445555", null, null, "", "User with null role and salary")
        );
    }
}