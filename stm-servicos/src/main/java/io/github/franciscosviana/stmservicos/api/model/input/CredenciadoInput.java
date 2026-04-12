package io.github.franciscosviana.stmservicos.api.model.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CredenciadoInput {

    private String rag;
    private Integer tipoPessoa;
    private String numeroPessoa;

    private BigDecimal valorChamado;
    private BigDecimal valorKm;

    private String tipoFluxoPagamento; // "MENSAL", "QUINZENAL", "SEMANAL", "UNICO"
    private String chavePix;

    private String contato;
    private String telefones;
    private String email;

    private EnderecoInput endereco;

}
