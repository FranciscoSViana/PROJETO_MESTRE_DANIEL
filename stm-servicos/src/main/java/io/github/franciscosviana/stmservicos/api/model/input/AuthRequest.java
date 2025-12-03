package io.github.franciscosviana.stmservicos.api.model.input;

import lombok.Data;

@Data
public class AuthRequest {

    private String usuario;
    private String senha;
}
