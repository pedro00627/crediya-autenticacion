package co.com.pragma.api.dto.response;

import java.time.LocalDate;

public record UserResponseRecord(
        String id,
        String firstName,
        String lastName,
        LocalDate birthDate,
        String email,
        String identityDocument,
        String phone,
        String roleId,
        Double baseSalary
) {
}