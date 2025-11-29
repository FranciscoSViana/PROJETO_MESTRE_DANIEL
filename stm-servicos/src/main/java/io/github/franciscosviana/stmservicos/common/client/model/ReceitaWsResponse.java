package io.github.franciscosviana.stmservicos.common.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ReceitaWsResponse {

    @JsonProperty("razao_social")
    private String razaoSocial;

    @JsonProperty("nome_fantasia")
    private String nomeFantasia;
    private String cnpj;

    @JsonProperty("descricao_situacao_cadastral")
    private String situacaoCadastral;
}
