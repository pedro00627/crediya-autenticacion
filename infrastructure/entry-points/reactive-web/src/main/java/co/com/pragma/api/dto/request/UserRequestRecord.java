package co.com.pragma.api.dto.request;

import co.com.pragma.model.constants.ErrorMessages;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record UserRequestRecord(
        @NotBlank(message = ErrorMessages.FIRST_NAME_REQUIRED)
        String firstName,
        @NotBlank(message = ErrorMessages.LAST_NAME_REQUIRED)
        String lastName,
        @NotNull(message = ErrorMessages.BIRTH_DATE_REQUIRED)
        @Past
        LocalDate birthDate,
        @NotBlank(message = ErrorMessages.EMAIL_FIELD_REQUIRED)
        @Email
        String email,
        @NotBlank(message = ErrorMessages.IDENTITY_DOCUMENT_REQUIRED)
        String identityDocument,
        @NotBlank(message = ErrorMessages.PHONE_REQUIRED)
        String phone,
        @NotBlank(message = ErrorMessages.ROLE_ID_REQUIRED)
        String roleId,
        @NotNull(message = ErrorMessages.BASE_SALARY_REQUIRED)
        Double baseSalary,
        @NotBlank(message = ErrorMessages.PASSWORD_REQUIRED)
        String password
) {
}