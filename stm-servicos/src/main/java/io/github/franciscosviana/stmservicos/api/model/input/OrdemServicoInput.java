package io.github.franciscosviana.stmservicos.api.model.input;

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
public class OrdemServicoInput {

    // Dados principais da OS
    private String osClt;
    private String osg;
    private String status;
    private OffsetDateTime dataHora;

    // Relacionamentos (apenas IDs)
    private UUID clienteId;
    private UUID credenciadoId;

    // Informações administrativas
    private String contrato;
    private String contato;
    private String departamento;
    private String telefone;

    // Endereço
    private EnderecoInput endereco;

    // Dados técnicos
    private String acionador;
    private String equipamento;
    private String serie;
    private String pib;
    private String defeito;
    private String rastreio;

    // Campos opcionais - NÃO enviados no cadastro
    private SolucaoOSInput solucao;       // usado apenas ao fechar OS
    private FaturamentoOSInput faturamento; // usado somente na etapa administrativa
}
