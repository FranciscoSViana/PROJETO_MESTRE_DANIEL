package io.github.franciscosviana.stmservicos.domain.model;

import io.github.franciscosviana.stmservicos.api.model.input.OrdemServicoInput;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdemServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String osClt;

    @Column(unique = true)
    private String osg;
    private String status;
    private String rag;

    private OffsetDateTime dataHora;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    private Credenciado credenciado;

    private String contrato;
    private String contato;
    private String departamento;
    private String telefone;

    @Embedded
    private Endereco endereco;
//    private String cidade;
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

    public static OrdemServico from(OrdemServicoInput input) {
        if (input == null) return null;

        OrdemServico os = OrdemServico.builder()
                .osClt(input.getOsClt())
                .osg(input.getOsg())
                .status(input.getStatus())
                .rag(input.getRag())
                .dataHora(input.getDataHora())
                .cliente(input.getClienteId() != null ? Cliente.builder()
                        .id(input.getClienteId())
                        .build() : null)
                .credenciado(input.getCredenciadoId() != null ? Credenciado.builder()
                        .id(input.getCredenciadoId())
                        .build() : null)
                .contrato(input.getContrato())
                .contato(input.getContato())
                .departamento(input.getDepartamento())
                .telefone(input.getTelefone())
                .endereco(Endereco.from(input.getEndereco()))
//                .cidade(input.getCidade())
                .acionador(input.getAcionador())
                .equipamento(input.getEquipamento())
                .serie(input.getSerie())
                .pib(input.getPib())
                .defeito(input.getDefeito())
                .rastreio(input.getRastreio())
                .build();

        // Solução
        if (input.getSolucao() != null) {
            SolucaoOS solucao = SolucaoOS.from(input.getSolucao());
            solucao.setOrdemServico(os); // vínculo bidirecional
            os.setSolucao(solucao);
        }

        // Faturamento
        if (input.getFaturamento() != null) {
            FaturamentoOS faturamento = FaturamentoOS.from(input.getFaturamento());
            faturamento.setOrdemServico(os); // vínculo bidirecional
            os.setFaturamento(faturamento);
        }

        return os;
    }

}
