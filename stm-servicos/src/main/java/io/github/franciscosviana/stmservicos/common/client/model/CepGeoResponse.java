package io.github.franciscosviana.stmservicos.common.client.model;

import lombok.Data;

@Data
public class CepGeoResponse {

    private String cep;
    private String city;
    private String state;
    private Double latitude;
    private Double longitude;
}
