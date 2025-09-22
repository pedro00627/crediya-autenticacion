package co.com.pragma.r2dbc.mapper;

import co.com.pragma.model.user.User;
import co.com.pragma.r2dbc.entity.UserEntity;
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
class UserDataMapperTest {

    private UserDataMapper mapper;

    @BeforeEach
    void setUp() {
        this.mapper = Mappers.getMapper(UserDataMapper.class);
    }

    @Test
    void shouldMapEntityToDomain() {
        // Arrange
        final UserEntity entity = new UserEntity(
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
        final User domain = this.mapper.toDomain(entity);

        // Assert
        assertNotNull(domain);
        assertEquals(entity.getId(), domain.id());
        assertEquals(entity.getFirstName(), domain.firstName());
        assertEquals(entity.getLastName(), domain.lastName());
        assertEquals(entity.getBirthDate(), domain.birthDate());
        assertEquals(entity.getEmail(), domain.email());
        assertEquals(entity.getIdentityDocument(), domain.identityDocument());
        assertEquals(entity.getPhone(), domain.phone());
        assertEquals(entity.getRoleId(), domain.roleId());
        assertEquals(entity.getBaseSalary(), domain.baseSalary());
        assertEquals(entity.getPassword(), domain.password());
    }

    @Test
    void shouldMapDomainToEntity() {
        // Arrange
        final User domain = new User(
                "2",
                "Jane",
                "Smith",
                LocalDate.of(1985, 8, 20),
                "jane.smith@example.com",
                "987654321",
                "3109876543",
                2,
                75000.0,
                "hashedPassword"
        );

        // Act
        final UserEntity entity = this.mapper.toEntity(domain);

        // Assert
        assertNotNull(entity);
        assertEquals(domain.id(), entity.getId());
        assertEquals(domain.firstName(), entity.getFirstName());
        assertEquals(domain.lastName(), entity.getLastName());
        assertEquals(domain.birthDate(), entity.getBirthDate());
        assertEquals(domain.email(), entity.getEmail());
        assertEquals(domain.identityDocument(), entity.getIdentityDocument());
        assertEquals(domain.phone(), entity.getPhone());
        assertEquals(domain.roleId(), entity.getRoleId());
        assertEquals(domain.baseSalary(), entity.getBaseSalary());
        assertEquals(domain.password(), entity.getPassword());
    }

    @ParameterizedTest
    @MethodSource("entityToDomainTestCases")
    void shouldMapEntityToDomainWithDifferentValues(final UserEntity entity, final String scenario) {
        // Act
        final User domain = this.mapper.toDomain(entity);

        // Assert
        assertNotNull(domain, scenario);
        assertEquals(entity.getId(), domain.id(), scenario);
        assertEquals(entity.getFirstName(), domain.firstName(), scenario);
        assertEquals(entity.getLastName(), domain.lastName(), scenario);
        assertEquals(entity.getBirthDate(), domain.birthDate(), scenario);
        assertEquals(entity.getEmail(), domain.email(), scenario);
        assertEquals(entity.getIdentityDocument(), domain.identityDocument(), scenario);
        assertEquals(entity.getPhone(), domain.phone(), scenario);
        assertEquals(entity.getRoleId(), domain.roleId(), scenario);
        assertEquals(entity.getBaseSalary(), domain.baseSalary(), scenario);
        assertEquals(entity.getPassword(), domain.password(), scenario);
    }

    @ParameterizedTest
    @MethodSource("domainToEntityTestCases")
    void shouldMapDomainToEntityWithDifferentValues(final User domain, final String scenario) {
        // Act
        final UserEntity entity = this.mapper.toEntity(domain);

        // Assert
        assertNotNull(entity, scenario);
        assertEquals(domain.id(), entity.getId(), scenario);
        assertEquals(domain.firstName(), entity.getFirstName(), scenario);
        assertEquals(domain.lastName(), entity.getLastName(), scenario);
        assertEquals(domain.birthDate(), entity.getBirthDate(), scenario);
        assertEquals(domain.email(), entity.getEmail(), scenario);
        assertEquals(domain.identityDocument(), entity.getIdentityDocument(), scenario);
        assertEquals(domain.phone(), entity.getPhone(), scenario);
        assertEquals(domain.roleId(), entity.getRoleId(), scenario);
        assertEquals(domain.baseSalary(), entity.getBaseSalary(), scenario);
        assertEquals(domain.password(), entity.getPassword(), scenario);
    }

    @Test
    void shouldHandleNullEntityFields() {
        // Arrange
        final UserEntity entityWithNulls = new UserEntity(
                null, null, null, null, null, null, null, null, null, null
        );

        // Act
        final User domain = this.mapper.toDomain(entityWithNulls);

        // Assert
        assertNotNull(domain);
        assertNull(domain.id());
        assertNull(domain.firstName());
        assertNull(domain.lastName());
        assertNull(domain.birthDate());
        assertNull(domain.email());
        assertNull(domain.identityDocument());
        assertNull(domain.phone());
        assertNull(domain.roleId());
        assertNull(domain.baseSalary());
        assertNull(domain.password());
    }

    @Test
    void shouldHandleNullDomainFields() {
        // Arrange
        final User domainWithNulls = new User(
                null, null, null, null, null, null, null, null, null, null
        );

        // Act
        final UserEntity entity = this.mapper.toEntity(domainWithNulls);

        // Assert
        assertNotNull(entity);
        assertNull(entity.getId());
        assertNull(entity.getFirstName());
        assertNull(entity.getLastName());
        assertNull(entity.getBirthDate());
        assertNull(entity.getEmail());
        assertNull(entity.getIdentityDocument());
        assertNull(entity.getPhone());
        assertNull(entity.getRoleId());
        assertNull(entity.getBaseSalary());
        assertNull(entity.getPassword());
    }

    @Test
    void shouldMaintainDataIntegrityInBidirectionalMapping() {
        // Arrange
        final User originalDomain = new User(
                "3",
                "Test",
                "User",
                LocalDate.of(1992, 12, 31),
                "test.user@example.com",
                "555666777",
                "3155556666",
                3,
                90000.0,
                "securePassword"
        );

        // Act
        final UserEntity entity = this.mapper.toEntity(originalDomain);
        final User mappedBackDomain = this.mapper.toDomain(entity);

        // Assert
        assertEquals(originalDomain, mappedBackDomain, "Bidirectional mapping should maintain data integrity");
    }

    @Test
    void shouldHandleSpecialCharactersInFields() {
        // Arrange
        final UserEntity entityWithSpecialChars = new UserEntity(
                "special-id-123",
                "José María",
                "O'Connor-García",
                LocalDate.of(1988, 2, 29),
                "josé.maría@email.com",
                "12.345.678-9",
                "+57 301 234 5678",
                1,
                1500000.50,
                "P@$$w0rd!2023"
        );

        // Act
        final User domain = this.mapper.toDomain(entityWithSpecialChars);

        // Assert
        assertNotNull(domain);
        assertEquals("José María", domain.firstName());
        assertEquals("O'Connor-García", domain.lastName());
        assertEquals("josé.maría@email.com", domain.email());
        assertEquals("12.345.678-9", domain.identityDocument());
        assertEquals("+57 301 234 5678", domain.phone());
        assertEquals(1500000.50, domain.baseSalary());
    }

    static Stream<Arguments> entityToDomainTestCases() {
        return Stream.of(
                Arguments.of(
                        new UserEntity("admin-1", "Admin", "User", LocalDate.of(1980, 1, 1),
                                     "admin@example.com", "111222333", "3201112222", 3, 100000.0, "adminPass"),
                        "Admin entity to domain mapping"
                ),
                Arguments.of(
                        new UserEntity("client-1", "Client", "User", LocalDate.of(1995, 12, 25),
                                     "client@example.com", "444555666", "3154445555", 1, 60000.0, "clientPass"),
                        "Client entity to domain mapping"
                ),
                Arguments.of(
                        new UserEntity("advisor-1", "Advisor", "User", LocalDate.of(1988, 6, 15),
                                     "advisor@example.com", "777888999", "3187778888", 2, 80000.0, "advisorPass"),
                        "Advisor entity to domain mapping"
                ),
                Arguments.of(
                        new UserEntity(null, "No", "ID", LocalDate.now(),
                                     "noid@example.com", "000111222", "3200001111", null, null, ""),
                        "Entity with null ID and role"
                )
        );
    }

    static Stream<Arguments> domainToEntityTestCases() {
        return Stream.of(
                Arguments.of(
                        new User("new-admin-1", "New", "Admin", LocalDate.of(1975, 3, 10),
                               "newadmin@example.com", "111000111", "3211110000", 3, 120000.0, "newAdminPass"),
                        "New admin domain to entity mapping"
                ),
                Arguments.of(
                        new User("new-client-1", "New", "Client", LocalDate.of(2000, 7, 4),
                               "newclient@example.com", "222000222", "3222220000", 1, 45000.0, "newClientPass"),
                        "New client domain to entity mapping"
                ),
                Arguments.of(
                        new User("new-advisor-1", "New", "Advisor", LocalDate.of(1992, 11, 30),
                               "newadvisor@example.com", "333000333", "3233330000", 2, 70000.0, "newAdvisorPass"),
                        "New advisor domain to entity mapping"
                ),
                Arguments.of(
                        new User("empty-role-1", "Empty", "Role", LocalDate.now(),
                               "emptyrole@example.com", "999000999", "3299990000", null, 50000.0, "emptyRolePass"),
                        "Domain with null role"
                )
        );
    }
}