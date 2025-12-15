package io.github.franciscosviana.stmservicos.api.model.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdemServicoOutput {

    private UUID id;
    private String osClt;
    private String osg;
    private String status;
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
