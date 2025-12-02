package io.github.franciscosviana.stmservicos.api.model.output;

import io.github.franciscosviana.stmservicos.domain.model.Endereco;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnderecoOutput {

    private String cep;
    private String logradouro;
    private String bairro;
    private String numero;
    private String complemento;

    public static EnderecoOutput from(Endereco e) {
        if (e == null) return null;
        return EnderecoOutput.builder()
                .cep(e.getCep())
                .logradouro(e.getLogradouro())
                .bairro(e.getBairro())
                .numero(e.getNumero())
                .complemento(e.getComplemento())
                .build();
    }
}
