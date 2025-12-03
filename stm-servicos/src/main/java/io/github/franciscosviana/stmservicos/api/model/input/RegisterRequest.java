package io.github.franciscosviana.stmservicos.api.model.input;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class RegisterRequest {

    @NotBlank
    private String usuario;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String senha;

    private Set<String> roles;
}
