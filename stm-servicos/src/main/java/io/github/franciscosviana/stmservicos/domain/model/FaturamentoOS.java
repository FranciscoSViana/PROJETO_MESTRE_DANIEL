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
@Table(name = "faturamento_os")
public class FaturamentoOS {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "ordem_servico_id")
    private OrdemServico ordemServico;

    private String cliente;
    private String contrato;

    @Column(name = "status_os")
    private String statusOs;
    private String rag;

    private String chamado;
    private BigDecimal deslocamento;

    @Column(name = "ttl_km")
    private BigDecimal ttlKm;
    private BigDecimal pedagios;
    private BigDecimal estac;
    private String outros;

    private BigDecimal total;
    private String doc;
    private String status;

    private BigDecimal km;

    @Column(name = "total_geral")
    private BigDecimal totalGeral;

    @Column(name = "nota_fiscal")
    private String notaFiscal;

    private BigDecimal faturado;

    @Column(name = "saldo_mo")
    private BigDecimal saldoMO;

    @Column(name = "saldo_km")
    private BigDecimal saldoKm;

    @Column(name = "saldo_outros")
    private BigDecimal saldoOutros;
    private BigDecimal imposto;

    @Column(name = "saldo_total")
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
