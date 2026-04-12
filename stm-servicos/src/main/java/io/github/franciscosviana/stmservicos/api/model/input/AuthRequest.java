package io.github.franciscosviana.stmservicos.api.model.input;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequest {

    @NotBlank
    private String usuario;

    @NotBlank
    private String senha;
}
