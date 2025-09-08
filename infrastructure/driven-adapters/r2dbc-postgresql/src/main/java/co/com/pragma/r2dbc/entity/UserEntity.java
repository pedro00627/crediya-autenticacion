package co.com.pragma.r2dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("usuario")
public class UserEntity {
    @Id
    @Column("id_usuario")
    private String id;
    @Column("nombre")
    private String firstName;
    @Column("apellido")
    private String lastName;
    @Column("fecha_nacimiento")
    private LocalDate birthDate;
    @Column("correo_electronico")
    private String email;
    @Column("documento_identidad")
    private String identityDocument;
    @Column("telefono")
    private String phone;
    @Column("id_rol")
    private Integer roleId;
    @Column("salario_base")
    private Double baseSalary;
    @Column("password")
    private String password;
}