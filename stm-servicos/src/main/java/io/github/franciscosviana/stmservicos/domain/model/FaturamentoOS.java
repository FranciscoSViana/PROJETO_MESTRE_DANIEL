package io.github.franciscosviana.stmservicos.domain.model;

import io.github.franciscosviana.stmservicos.api.model.input.FaturamentoOSInput;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaturamentoOS {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private OrdemServico ordemServico;

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

    public static FaturamentoOS from(FaturamentoOSInput input) {
        if (input == null) return null;

        return FaturamentoOS.builder()
                .cliente(input.getCliente())
                .contrato(input.getContrato())
                .statusOs(input.getStatusOs())
                .rag(input.getRag())
                .chamado(input.getChamado())
                .deslocamento(input.getDeslocamento())
                .ttlKm(input.getTtlKm())
                .pedagios(input.getPedagios())
                .estac(input.getEstac())
                .outros(input.getOutros())
                .total(input.getTotal())
                .doc(input.getDoc())
                .status(input.getStatus())
                .km(input.getKm())
                .totalGeral(input.getTotalGeral())
                .notaFiscal(input.getNotaFiscal())
                .faturado(input.getFaturado())
                .saldoMO(input.getSaldoMO())
                .saldoKm(input.getSaldoKm())
                .saldoOutros(input.getSaldoOutros())
                .imposto(input.getImposto())
                .saldoTotal(input.getSaldoTotal())
                .build();
    }
}
