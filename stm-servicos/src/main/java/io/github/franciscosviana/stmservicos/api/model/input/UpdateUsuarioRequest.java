package io.github.franciscosviana.stmservicos.api.model.input;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class UpdateUsuarioRequest {

    @Size(min = 3, max = 150)
    private String nomeCompleto;

    @Email
    private String email;

    @Past
    private LocalDate dataNascimento;

    private Set<String> roles;
}