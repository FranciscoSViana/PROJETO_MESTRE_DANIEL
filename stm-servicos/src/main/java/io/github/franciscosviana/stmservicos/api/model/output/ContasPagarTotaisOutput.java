package io.github.franciscosviana.stmservicos.api.model.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContasPagarTotaisOutput {

    // ── Totais gerais ─────────────────────────────────────────────────
    private BigDecimal totalGeral;
    private BigDecimal totalPago;
    private BigDecimal totalNaoPago;

    private long qtdTotal;
    private long qtdPago;
    private long qtdNaoPago;

    // ── Destrinchado — PAGOS ──────────────────────────────────────────
    private BigDecimal pagoValorChamado;
    private BigDecimal pagoKmTotal;       // soma dos km percorridos
    private BigDecimal pagoValorKm;       // soma do custo de km (km × valorKm)
    private BigDecimal pagoPedagio;
    private BigDecimal pagoEstacionamento;
    private BigDecimal pagoOutros;

    // ── Destrinchado — NÃO PAGOS ─────────────────────────────────────
    private BigDecimal naoPagoValorChamado;
    private BigDecimal naoPagoKmTotal;
    private BigDecimal naoPagoValorKm;
    private BigDecimal naoPagoPedagio;
    private BigDecimal naoPagoEstacionamento;
    private BigDecimal naoPagoOutros;
}