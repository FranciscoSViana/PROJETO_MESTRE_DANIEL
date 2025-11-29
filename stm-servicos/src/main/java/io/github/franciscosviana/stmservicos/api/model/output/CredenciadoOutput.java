package io.github.franciscosviana.stmservicos.api.model.output;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CredenciadoOutput {

    private UUID id;

    private Long codigo;

    private String rag;
    private String cidade;
    private String uf;
    private String tipo;

    private double valorChamado;
    private double valorKm;

    private int quantidadeOSAtendidas;

    private String contato;
    private String telefones;
    private String email;

    private String tecnico;
    private String cpf;
    private String base;
}
