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

    // =========================
    // ENDEREÇO
    // =========================

    private String uf;

    private String cep;

    private String bairro;

    private String municipio;

    private String logradouro;

    @JsonProperty("descricao_tipo_de_logradouro")
    private String tipoLogradouro;

    private String numero;

    private String complemento;
}
