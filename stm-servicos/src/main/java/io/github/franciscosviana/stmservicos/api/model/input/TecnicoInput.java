package io.github.franciscosviana.stmservicos.api.model.input;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TecnicoInput {


    private String nome;
    private String cpf;
    private String telefone;
    private String email;
    private EnderecoInput endereco;
}
