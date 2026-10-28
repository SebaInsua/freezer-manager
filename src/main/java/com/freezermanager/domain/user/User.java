
package com.freezermanager.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El email es obligatorio.")
    @Email(message = "El email debe tener un formato válido.")
    @Size(max = 254, message = "El email no puede superar los 254 caracteres.")
    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @NotBlank(message = "La contraseña es obligatoria.")
    @Column(nullable = false)
    private String password;
}
