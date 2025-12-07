package io.github.franciscosviana.stmservicos.api.model.input;

import lombok.Data;

import java.util.Set;

@Data
public class UpdateUsuarioRequest {

    private String nome;
    private String email;
    private Set<String> roles;
}
