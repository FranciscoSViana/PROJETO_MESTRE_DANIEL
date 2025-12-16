package io.github.franciscosviana.stmservicos.domain.model;

import io.github.franciscosviana.stmservicos.domain.model.enums.StatusOrdem;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdemServico {

    @Id
    private UUID id;

    private String osClt;

    @Column(unique = true)
    private String osg;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusOrdem status = StatusOrdem.ABERTA;

    private OffsetDateTime dataHora;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    private Credenciado credenciado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contrato contrato;
    private String contato;
    private String departamento;
    private String telefone;

    @Embedded
    private Endereco endereco;
    private String acionador;
    private String equipamento;
    private String serie;
    private String pib;
    private String defeito;
    private String rastreio;

    @OneToOne(mappedBy = "ordemServico", cascade = CascadeType.ALL)
    private SolucaoOS solucao;

    @OneToOne(mappedBy = "ordemServico", cascade = CascadeType.ALL)
    private FaturamentoOS faturamento;
}
