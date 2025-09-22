package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.request.UserRequestRecord;
import co.com.pragma.api.dto.response.UserResponseRecord;
import co.com.pragma.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class UserDTOMapperTest {

    private UserDTOMapper mapper;

    @BeforeEach
    void setUp() {
        this.mapper = Mappers.getMapper(UserDTOMapper.class);
    }

    @Test
    void shouldMapUserToResponseRecord() {
        // Arrange
        final User user = new User(
                "1",
                "John",
                "Doe",
                LocalDate.of(1990, 5, 15),
                "john.doe@example.com",
                "123456789",
                "3001234567",
                1,
                50000.0,
                "hashedPassword"
        );

        // Act
        final UserResponseRecord response = this.mapper.toResponse(user);

        // Assert
        assertNotNull(response);
        assertEquals(user.id(), response.id());
        assertEquals(user.firstName(), response.firstName());
        assertEquals(user.lastName(), response.lastName());
        assertEquals(user.birthDate(), response.birthDate());
        assertEquals(user.email(), response.email());
        assertEquals(user.identityDocument(), response.identityDocument());
        assertEquals(user.phone(), response.phone());
        assertEquals(user.roleId().toString(), response.roleId());
        assertEquals(user.baseSalary(), response.baseSalary());
    }

    @Test
    void shouldMapRequestRecordToUserModel() {
        // Arrange
        final UserRequestRecord request = new UserRequestRecord(
                "Jane",
                "Smith",
                LocalDate.of(1985, 8, 20),
                "jane.smith@example.com",
                "987654321",
                "3109876543",
                "2",
                75000.0,
                "plainPassword"
        );

        // Act
        final User user = this.mapper.toModel(request);

        // Assert
        assertNotNull(user);
        assertNull(user.id(), "ID should be null as it's ignored in mapping");
        assertEquals(request.firstName(), user.firstName());
        assertEquals(request.lastName(), user.lastName());
        assertEquals(request.birthDate(), user.birthDate());
        assertEquals(request.email(), user.email());
        assertEquals(request.identityDocument(), user.identityDocument());
        assertEquals(request.phone(), user.phone());
        assertEquals(Integer.parseInt(request.roleId()), user.roleId());
        assertEquals(request.baseSalary(), user.baseSalary());
        assertEquals(request.password(), user.password());
    }

    @ParameterizedTest
    @MethodSource("userToResponseTestCases")
    void shouldMapUserToResponseWithDifferentValues(final User user, final String scenario) {
        // Act
        final UserResponseRecord response = this.mapper.toResponse(user);

        // Assert
        assertNotNull(response, scenario);
        assertEquals(user.id(), response.id(), scenario);
        assertEquals(user.firstName(), response.firstName(), scenario);
        assertEquals(user.lastName(), response.lastName(), scenario);
        assertEquals(user.birthDate(), response.birthDate(), scenario);
        assertEquals(user.email(), response.email(), scenario);
        assertEquals(user.identityDocument(), response.identityDocument(), scenario);
        assertEquals(user.phone(), response.phone(), scenario);
        assertEquals(null != user.roleId() ? user.roleId().toString() : null, response.roleId(), scenario);
        assertEquals(user.baseSalary(), response.baseSalary(), scenario);
    }

    @ParameterizedTest
    @MethodSource("requestToModelTestCases")
    void shouldMapRequestToModelWithDifferentValues(final UserRequestRecord request, final String scenario) {
        // Act
        final User user = this.mapper.toModel(request);

        // Assert
        assertNotNull(user, scenario);
        assertNull(user.id(), "ID should always be null in mapping: " + scenario);
        assertEquals(request.firstName(), user.firstName(), scenario);
        assertEquals(request.lastName(), user.lastName(), scenario);
        assertEquals(request.birthDate(), user.birthDate(), scenario);
        assertEquals(request.email(), user.email(), scenario);
        assertEquals(request.identityDocument(), user.identityDocument(), scenario);
        assertEquals(request.phone(), user.phone(), scenario);
        assertEquals(null != request.roleId() ? Integer.parseInt(request.roleId()) : null, user.roleId(), scenario);
        assertEquals(request.baseSalary(), user.baseSalary(), scenario);
        assertEquals(request.password(), user.password(), scenario);
    }

    @Test
    void shouldHandleNullUserFields() {
        // Arrange
        final User userWithNulls = new User(
                null, null, null, null, null, null, null, null, null, null
        );

        // Act
        final UserResponseRecord response = this.mapper.toResponse(userWithNulls);

        // Assert
        assertNotNull(response);
        assertNull(response.id());
        assertNull(response.firstName());
        assertNull(response.lastName());
        assertNull(response.birthDate());
        assertNull(response.email());
        assertNull(response.identityDocument());
        assertNull(response.phone());
        assertNull(response.roleId());
        assertNull(response.baseSalary());
    }

    @Test
    void shouldHandleNullRequestFields() {
        // Arrange
        final UserRequestRecord requestWithNulls = new UserRequestRecord(
                null, null, null, null, null, null, null, null, null
        );

        // Act
        final User user = this.mapper.toModel(requestWithNulls);

        // Assert
        assertNotNull(user);
        assertNull(user.id());
        assertNull(user.firstName());
        assertNull(user.lastName());
        assertNull(user.birthDate());
        assertNull(user.email());
        assertNull(user.identityDocument());
        assertNull(user.phone());
        assertNull(user.roleId());
        assertNull(user.baseSalary());
        assertNull(user.password());
    }

    @Test
    void shouldMapRoleIdCorrectlyFromStringToInteger() {
        // Arrange
        final UserRequestRecord request = new UserRequestRecord(
                "Test", "User", LocalDate.now(), "test@example.com",
                "123456789", "3001234567", "3", 50000.0, "password"
        );

        // Act
        final User user = this.mapper.toModel(request);

        // Assert
        assertEquals(3, user.roleId());
    }

    @Test
    void shouldMapRoleIdCorrectlyFromIntegerToString() {
        // Arrange
        final User user = new User(
                "1", "Test", "User", LocalDate.now(), "test@example.com",
                "123456789", "3001234567", 3, 50000.0, "password"
        );

        // Act
        final UserResponseRecord response = this.mapper.toResponse(user);

        // Assert
        assertEquals("3", response.roleId());
    }

    static Stream<Arguments> userToResponseTestCases() {
        return Stream.of(
                Arguments.of(
                        new User("1", "Admin", "User", LocalDate.of(1980, 1, 1),
                               "admin@example.com", "111222333", "3201112222", 3, 100000.0, "adminPass"),
                        "Admin user mapping"
                ),
                Arguments.of(
                        new User("2", "Client", "User", LocalDate.of(1995, 12, 25),
                               "client@example.com", "444555666", "3154445555", 1, 60000.0, "clientPass"),
                        "Client user mapping"
                ),
                Arguments.of(
                        new User("3", "Advisor", "User", LocalDate.of(1988, 6, 15),
                               "advisor@example.com", "777888999", "3187778888", 2, 80000.0, "advisorPass"),
                        "Advisor user mapping"
                ),
                Arguments.of(
                        new User(null, "No", "ID", LocalDate.now(),
                               "noid@example.com", "000111222", "3200001111", null, null, ""),
                        "User with null ID and role"
                )
        );
    }

    static Stream<Arguments> requestToModelTestCases() {
        return Stream.of(
                Arguments.of(
                        new UserRequestRecord("New", "Admin", LocalDate.of(1975, 3, 10),
                                            "newadmin@example.com", "111000111", "3211110000", "3", 120000.0, "newAdminPass"),
                        "New admin request mapping"
                ),
                Arguments.of(
                        new UserRequestRecord("New", "Client", LocalDate.of(2000, 7, 4),
                                            "newclient@example.com", "222000222", "3222220000", "1", 45000.0, "newClientPass"),
                        "New client request mapping"
                ),
                Arguments.of(
                        new UserRequestRecord("New", "Advisor", LocalDate.of(1992, 11, 30),
                                            "newadvisor@example.com", "333000333", "3233330000", "2", 70000.0, "newAdvisorPass"),
                        "New advisor request mapping"
                ),
                Arguments.of(
                        new UserRequestRecord("Empty", "Role", LocalDate.now(),
                                            "emptyrole@example.com", "999000999", "3299990000", null, 50000.0, "emptyRolePass"),
                        "Request with null role"
                )
        );
    }
}