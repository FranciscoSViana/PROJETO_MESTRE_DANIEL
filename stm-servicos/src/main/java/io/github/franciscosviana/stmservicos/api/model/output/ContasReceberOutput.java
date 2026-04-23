package io.github.franciscosviana.stmservicos.api.model.output;

import io.github.franciscosviana.stmservicos.domain.model.enums.StatusOrdem;
import io.github.franciscosviana.stmservicos.domain.model.enums.TipoPagamento;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ContasReceberOutput {

    private UUID ordemServicoId;
    private String osg;
    private String osClt;
    private String cliente;
    private String contrato;

    private StatusOrdem statusOrdem;
    private OffsetDateTime dataHoraAbertura;

    private BigDecimal valorChamado;
    private BigDecimal km;
    private BigDecimal valorKm;
    private BigDecimal pedagio;
    private BigDecimal estacionamento;
    private BigDecimal valorOutros;
    private BigDecimal valorTotal;

    private boolean pago;
    private boolean corrigido;
    private TipoPagamento tipoPagamento;
    private String banco;
    private String nf;
    private String lote;
    private String urlComprovante;
    private LocalDate dataPrevista;
    private OffsetDateTime dataPagamento;
    private OffsetDateTime criadoEm;
}