package co.com.pragma.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record UserRequestRecord(
        @NotBlank(message = "El primer nombre no puede estar vacío")
        String firstName,
        @NotBlank(message = "El apellido no puede estar vacío")
        String lastName,
        @NotNull(message = "La fecha de nacimiento no puede ser nula")
        @Past
        LocalDate birthDate,
        @NotBlank(message = "El correo electrónico no puede estar vacío")
        @Email
        String email,
        @NotBlank(message = "El documento de identidad no puede estar vacío")
        String identityDocument,
        @NotBlank(message = "El teléfono no puede estar vacío")
        String phone,
        @NotBlank(message = "El ID del rol no puede estar vacío")
        String roleId,
        @NotNull(message = "El salario base no puede ser nulo")
        Double baseSalary,
        @NotBlank(message = "La contraseña no puede estar vacía")
        String password
) {
}