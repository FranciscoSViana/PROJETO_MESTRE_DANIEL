package io.github.franciscosviana.stmservicos.domain.model;

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
    private String osg;
    private String status;
    private String rag;
    private OffsetDateTime dataHora;

    @ManyToOne
    private Cliente cliente;

    @ManyToOne
    private Credenciado credenciado;

    private String contrato;
    private String contato;
    private String departamento;
    private String telefone;
    private String endereco;
    private String cidade;
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
