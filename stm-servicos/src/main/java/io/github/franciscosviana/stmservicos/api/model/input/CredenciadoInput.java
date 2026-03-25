package io.github.franciscosviana.stmservicos.api.model.input;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CredenciadoInput {

    private String rag;
    private Integer tipoPessoa;
    private String numeroPessoa;

    private BigDecimal valorChamado;
    private BigDecimal valorKm;

    private int quantidadeOSAtendidas;

    private String contato;
    private String telefones;
    private String email;

    private EnderecoInput endereco;

}
