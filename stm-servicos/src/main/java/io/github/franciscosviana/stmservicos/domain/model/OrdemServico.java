package io.github.franciscosviana.stmservicos.domain.model;

import io.github.franciscosviana.stmservicos.common.validation.OrdemServicoException;
import io.github.franciscosviana.stmservicos.domain.model.enums.StatusOrdem;
import io.github.franciscosviana.stmservicos.domain.model.enums.StatusRastreio;
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
    @GeneratedValue
    private UUID id;

    private String osClt;

    @Column(unique = true)
    private String osg;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusOrdem status = StatusOrdem.ABERTA;

    @Column(nullable = false)
    private OffsetDateTime dataHoraAbertura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "credenciado_id")
    private Credenciado credenciado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tecnico_id", nullable = false)
    private Tecnico tecnico;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "status_rastreio")
    private StatusRastreio statusRastreio;

    @OneToOne(mappedBy = "ordemServico", cascade = CascadeType.ALL)
    private SolucaoOS solucao;

    @OneToOne(mappedBy = "ordemServico", cascade = CascadeType.ALL)
    private FaturamentoOS faturamento;

    @OneToOne(mappedBy = "ordemServico", cascade = CascadeType.ALL)
    private PagamentoOS pagamento;

    public void concluirCom(SolucaoOS solucao) {

        if (this.status == StatusOrdem.CONCLUIDA) {
            throw new OrdemServicoException("Ordem já concluída");
        }

        solucao.setOrdemServico(this); // <- IMPORTANTE

        this.solucao = solucao;
        this.status = StatusOrdem.CONCLUIDA;
    }
}
