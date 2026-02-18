package io.github.franciscosviana.stmservicos.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "solucao_os")
public class SolucaoOS {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemServico ordemServico;

    // Dados do atendimento
    private OffsetDateTime dataAtendimento;
    private OffsetDateTime horaInicial;
    private OffsetDateTime horaFinal;

    @Column(columnDefinition = "TEXT")
    private String solucao;

    private String pecaSolicitada;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    // Custos
    private BigDecimal km;
    private BigDecimal pedagio;
    private BigDecimal estacionamento;
    private BigDecimal outros;
}

