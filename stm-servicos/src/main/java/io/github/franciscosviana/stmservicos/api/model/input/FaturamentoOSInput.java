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
public class FaturamentoOSInput {


    private String cliente;
    private String contrato;
    private String statusOs;
    private String rag;

    private String chamado;
    private BigDecimal deslocamento;
    private BigDecimal ttlKm;
    private BigDecimal pedagios;
    private BigDecimal estac;
    private String outros;

    private BigDecimal total;
    private String doc;
    private String status;

    private BigDecimal km;

    private BigDecimal totalGeral;
    private String notaFiscal;

    private BigDecimal faturado;
    private BigDecimal saldoMO;
    private BigDecimal saldoKm;
    private BigDecimal saldoOutros;
    private BigDecimal imposto;
    private BigDecimal saldoTotal;
}
