package io.github.franciscosviana.stmservicos.api.model.input;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Set;

@Data
public class RegisterRequest {

    @NotBlank(message = "Usuário é obrigatório")
    @Size(min = 3, max = 100, message = "Usuário deve ter entre 3 e 100 caracteres")
    private String nome;

    @Email(message = "Email inválido")
    @NotBlank(message = "Email é obrigatório")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$",
            message = "A senha deve conter no mínimo 8 caracteres, incluindo letras maiúsculas, minúsculas, números e caracteres especiais"
    )
    private String senha;

    @NotEmpty(message = "Perfil é obrigatório")
    private Set<String> roles;
}
