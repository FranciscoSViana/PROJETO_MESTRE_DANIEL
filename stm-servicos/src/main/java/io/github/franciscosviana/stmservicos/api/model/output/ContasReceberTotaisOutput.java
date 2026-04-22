package io.github.franciscosviana.stmservicos.api.model.output;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ContasReceberTotaisOutput {

    private long qtdTotal;
    private long qtdRecebido;
    private long qtdNaoRecebido;

    private BigDecimal totalRecebido;
    private BigDecimal totalNaoRecebido;
    private BigDecimal totalGeral;

    // destrinchado — recebido
    private BigDecimal recebidoValorChamado;
    private BigDecimal recebidoValorKm;
    private BigDecimal recebidoPedagio;
    private BigDecimal recebidoEstacionamento;
    private BigDecimal recebidoOutros;

    // destrinchado — não recebido
    private BigDecimal naoRecebidoValorChamado;
    private BigDecimal naoRecebidoValorKm;
    private BigDecimal naoRecebidoPedagio;
    private BigDecimal naoRecebidoEstacionamento;
    private BigDecimal naoRecebidoOutros;
}