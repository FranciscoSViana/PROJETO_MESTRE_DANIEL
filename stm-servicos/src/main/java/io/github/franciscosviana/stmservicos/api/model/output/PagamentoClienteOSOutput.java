package io.github.franciscosviana.stmservicos.api.model.output;

import io.github.franciscosviana.stmservicos.domain.model.enums.TipoPagamento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagamentoClienteOSOutput {

    private Long id;

    private String osClt;
    private String osg;
    private String cliente;
    private String contrato;

    private BigDecimal valorChamado;
    private BigDecimal km;
    private BigDecimal valorKm;
    private BigDecimal pedagio;
    private BigDecimal estacionamento;
    private String outros;
    private BigDecimal valorOutros;
    private BigDecimal valorTotal;

    private String lote;
    private String nf;
    private TipoPagamento tipoPagamento;
    private String banco;
    private String urlComprovante;

    private LocalDate dataPrevista;
    private OffsetDateTime dataPagamento;
    private OffsetDateTime criadoEm;

    private boolean recebido;
}