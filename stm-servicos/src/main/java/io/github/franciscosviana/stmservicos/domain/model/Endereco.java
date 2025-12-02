package io.github.franciscosviana.stmservicos.domain.model;

import io.github.franciscosviana.stmservicos.api.model.input.EnderecoInput;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Endereco {

    @Column(name = "endereco_cep")
    private String cep;

    @Column(name = "endereco_logradouro")
    private String logradouro;

    @Column(name = "endereco_numero")
    private String numero;

    @Column(name = "endereco_complemento")
    private String complemento;

    @Column(name = "endereco_bairro")
    private String bairro;

    @JoinColumn(name = "endereco_cidade")
    private String cidade;

    public static Endereco from(EnderecoInput input) {
        if (input == null) return null;

        return Endereco.builder()
                .cep(input.getCep())
                .logradouro(input.getLogradouro())
                .numero(input.getNumero())
                .complemento(input.getComplemento())
                .bairro(input.getBairro())
                .cidade(input.getCidade())
                .build();
    }
}
