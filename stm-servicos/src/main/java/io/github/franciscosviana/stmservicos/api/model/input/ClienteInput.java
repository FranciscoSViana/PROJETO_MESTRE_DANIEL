package io.github.franciscosviana.stmservicos.api.model.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteInput {

    private String nome;

    private List<ContratoInput> contratos;

    private BigDecimal valorChamado;
    private BigDecimal valorKm;

    private String cnpj;
    private String inscricaoEstadual;
    private String razaoSocial;

    private EnderecoInput endereco;
}
