package io.github.franciscosviana.stmservicos.api.model.output;

import lombok.Data;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
public class UsuarioResponse {

    private UUID id;
    private String nomeCompleto;
    private String username;
    private String email;
    private LocalDate dataNascimento;
    private Integer idade;
    private Set<String> roles;
    private boolean enabled;
}