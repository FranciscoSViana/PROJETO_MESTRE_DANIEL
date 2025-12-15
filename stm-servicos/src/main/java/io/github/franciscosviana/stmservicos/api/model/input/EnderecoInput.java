package io.github.franciscosviana.stmservicos.api.model.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnderecoInput {

    private String cep;
    private String logradouro;
    private String bairro;
    private String numero;
    private String complemento;
    private String cidade;
    private String estado;
}
