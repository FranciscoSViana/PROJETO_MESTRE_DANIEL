package io.github.franciscosviana.stmservicos.api.model.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteInput {

    private String contrato;
    private String nome;

    private double valorChamado;
    private double valorKm;

    private String cnpj;
    private String inscricaoEstadual;
    private String razaoSocial;
}
