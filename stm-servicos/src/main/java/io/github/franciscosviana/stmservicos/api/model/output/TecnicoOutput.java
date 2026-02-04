package io.github.franciscosviana.stmservicos.api.model.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TecnicoOutput {

    private UUID id;
    private Long codigo;
    private String nome;
    private String cpf;
    private String telefone;
    private String email;
}
