package io.github.franciscosviana.stmservicos.api.model.output;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ClienteOutput {

    private UUID id;

    private Long codigo;

    private String contrato;
    private String nome;

    private double valorChamado;
    private double valorKm;

    private String cnpj;
    private String inscricaoEstadual;
    private String razaoSocial;
}
