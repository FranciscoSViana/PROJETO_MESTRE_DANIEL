package io.github.franciscosviana.stmservicos.api.model.output;

import io.github.franciscosviana.stmservicos.domain.model.enums.StatusOrdem;
import io.github.franciscosviana.stmservicos.domain.model.enums.TipoFluxoPagamento;
import io.github.franciscosviana.stmservicos.domain.model.enums.TipoPagamento;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ContasPagarOutput {

    private UUID ordemServicoId;
    private String osg;
    private String osClt;
    private String cliente;
    private String contrato;

    // Credenciado
    private UUID credenciadoId;
    private Long credenciadoCodigo;
    private String credenciadoRag;
    private TipoFluxoPagamento tipoFluxoPagamento;

    // Status da OS
    private StatusOrdem statusOrdem;
    private OffsetDateTime dataHoraAbertura;

    // Valores calculados
    private BigDecimal valorChamado;
    private BigDecimal km;
    private BigDecimal valorKm;
    private BigDecimal pedagio;
    private BigDecimal estacionamento;
    private BigDecimal valorOutros;
    private BigDecimal valorTotal;

    // Pagamento
    private boolean pago;
    private TipoPagamento tipoPagamento;
    private String banco;
    private String chavePix;
    private String cpfNf;
    private String lote;
    private String urlComprovante;
    private OffsetDateTime dataPagamento;
    private OffsetDateTime pagamentoCriadoEm;
}