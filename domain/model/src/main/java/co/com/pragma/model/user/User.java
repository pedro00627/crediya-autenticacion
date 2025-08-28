package co.com.pragma.model.user;

import java.time.LocalDate;

public record User(
        String id,
        String firstName,
        String lastName,
        LocalDate birthDate,
        String email,
        String identityDocument,
        String phone,
        Integer roleId,
        Double baseSalary
) {
}
