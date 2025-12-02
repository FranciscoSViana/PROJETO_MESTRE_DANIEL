package io.github.franciscosviana.stmservicos.api.model.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdemServicoOutput {

    private Long id;
    private String osClt;
    private String osg;
    private String status;
    private String rag;
    private OffsetDateTime dataHora;

    private ClienteOutput cliente;
    private CredenciadoOutput credenciado;

    private String contrato;
    private String contato;
    private String departamento;
    private String telefone;

    private EnderecoOutput endereco;

    private String acionador;
    private String equipamento;
    private String serie;
    private String pib;
    private String defeito;
    private String rastreio;

    private SolucaoOSOutput solucao;          // pode ser null
    private FaturamentoOSOutput faturamento;  // pode ser null
}
