package io.github.franciscosviana.stmservicos.api.model.input;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class ContasPagarFilter {

    private String osg;
    private String osClt;
    private String cliente;
    private String credenciado;
    private Boolean pago;

    /**
     * Filtra pelo campo `lote` de PagamentoOS.
     * Era "tipoFluxoPagamento" mas o fluxo de pagamento na tabela pagamento_os é o campo lote.
     */
    private String lote;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataInicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataFim;
}