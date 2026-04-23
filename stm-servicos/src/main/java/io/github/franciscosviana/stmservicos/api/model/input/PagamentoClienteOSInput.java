package io.github.franciscosviana.stmservicos.api.model.input;

import io.github.franciscosviana.stmservicos.domain.model.enums.TipoPagamento;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoClienteOSInput {

    @DecimalMin(value = "0.0", message = "KM não pode ser negativo")
    private BigDecimal km;

    private BigDecimal valorChamado;
    private BigDecimal valorKm;

    @DecimalMin(value = "0.0", message = "Pedágio não pode ser negativo")
    private BigDecimal pedagio;

    @DecimalMin(value = "0.0", message = "Estacionamento não pode ser negativo")
    private BigDecimal estacionamento;

    private String outros;

    @DecimalMin(value = "0.0", message = "Valor outros não pode ser negativo")
    private BigDecimal valorOutros;

    private String lote;
    private String nf;

    private TipoPagamento tipoPagamento;
    private String banco;

    private String urlComprovante;

    private LocalDate dataPrevista;

    // ✅ LocalDate em vez de OffsetDateTime — recebe "yyyy-MM-dd" do frontend
    private LocalDate dataPagamento;
}