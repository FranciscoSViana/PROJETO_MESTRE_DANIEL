package io.github.franciscosviana.stmservicos.common.client.model;

import lombok.Data;

@Data
public class ViaCepResponse {

    private String cep;
    private String logradouro;
    private String complemento;
    private String bairro;
    private String localidade; // cidade
    private String uf;
}
