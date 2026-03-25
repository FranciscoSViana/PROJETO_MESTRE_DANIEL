package io.github.franciscosviana.stmservicos.api.model.input;

import io.github.franciscosviana.stmservicos.domain.model.enums.TipoPagamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoOSInput {

    // Quilometragem percorrida
    @DecimalMin(value = "0.0", message = "KM não pode ser negativo")
    private BigDecimal km;

    // Valor chamado: pode vir do cliente ou ser sobrescrito
    private BigDecimal valorChamado;

    // Valor por KM: pode vir do cliente ou ser sobrescrito
    private BigDecimal valorKm;

    @DecimalMin(value = "0.0", message = "Pedágio não pode ser negativo")
    private BigDecimal pedagio;

    @DecimalMin(value = "0.0", message = "Estacionamento não pode ser negativo")
    private BigDecimal estacionamento;

    // Descrição do item "outros"
    private String outros;

    @DecimalMin(value = "0.0", message = "Valor outros não pode ser negativo")
    private BigDecimal valorOutros;

    private String lote;

    private String cpfNf;

    @NotNull(message = "Tipo de pagamento é obrigatório")
    private TipoPagamento tipoPagamento;

    private String banco;

    // Data em que o pagamento foi efetuado (opcional; se nulo usa now())
    private java.time.OffsetDateTime dataPagamento;

    // URL do comprovante após upload — o front faz o upload e manda a URL
    private String urlComprovante;

    // Apenas usado no endpoint de edição para marcar como pago/não pago
    private Boolean pago;

    // ✅ Inserir após o campo `banco`:
    private String chavePix;
}