package io.github.franciscosviana.stmservicos.api.model.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteInput {

    private String contrato;
    private String nome;

    private BigDecimal valorChamado;
    private BigDecimal valorKm;

    private String cnpj;
    private String inscricaoEstadual;
    private String razaoSocial;
}
