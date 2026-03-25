package io.github.franciscosviana.stmservicos.api.model.output;

import io.github.franciscosviana.stmservicos.domain.model.enums.TipoPagamento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoOSOutput {

    private Long id;

    // Dados da OS (desnormalizados para leitura rápida)
    private String osClt;
    private String osg;
    private String cliente;
    private String contrato;

    // Detalhamento dos valores
    private BigDecimal valorChamado;
    private BigDecimal km;
    private BigDecimal valorKm;
    private BigDecimal pedagio;
    private BigDecimal estacionamento;
    private String outros;
    private BigDecimal valorOutros;

    // Total calculado automaticamente
    private BigDecimal valorTotal;

    // Dados de pagamento
    private String lote;
    private String cpfNf;
    private TipoPagamento tipoPagamento;
    private String banco;
    private String urlComprovante;

    private OffsetDateTime dataPagamento;
    private OffsetDateTime criadoEm;

    private boolean pago;

    // ✅ Inserir após o campo `banco`:
    private String chavePix;
}