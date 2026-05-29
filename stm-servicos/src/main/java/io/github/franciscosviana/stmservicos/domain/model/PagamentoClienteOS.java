package io.github.franciscosviana.stmservicos.domain.model;

import io.github.franciscosviana.stmservicos.domain.model.enums.TipoPagamento;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pagamento_cliente_os")
public class PagamentoClienteOS {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "ordem_servico_id", nullable = false, unique = true)
    @NotNull
    private OrdemServico ordemServico;

    // Snapshot
    @Column(name = "os_clt")
    private String osClt;

    private String osg;
    private String cliente;
    private String contrato;

    // Valores
    @Builder.Default
    @PositiveOrZero
    @Column(name = "valor_chamado", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorChamado = BigDecimal.ZERO;

    @Builder.Default
    @PositiveOrZero
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal km = BigDecimal.ZERO;

    @Builder.Default
    @PositiveOrZero
    @Column(name = "valor_km", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorKm = BigDecimal.ZERO;

    @Builder.Default
    @PositiveOrZero
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pedagio = BigDecimal.ZERO;

    @Builder.Default
    @PositiveOrZero
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal estacionamento = BigDecimal.ZERO;

    private String outros;

    @Builder.Default
    @PositiveOrZero
    @Column(name = "valor_outros", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorOutros = BigDecimal.ZERO;

    @Builder.Default
    @PositiveOrZero
    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    private String lote;

    @Column(name = "nf")
    private String nf;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pagamento", length = 50)
    private TipoPagamento tipoPagamento;

    private String banco;

    @Column(name = "url_comprovante")
    private String urlComprovante;

    @Column(name = "data_prevista")
    private LocalDate dataPrevista;

    @Column(name = "data_pagamento")
    private OffsetDateTime dataPagamento;

    @Column(name = "criado_em", insertable = false, updatable = false)
    private OffsetDateTime criadoEm;

    @Column(name = "pago", nullable = false)
    @Builder.Default
    private boolean pago = false;

    @Builder.Default
    @Column(name = "corrigido", nullable = false)
    private boolean corrigido = false;
}