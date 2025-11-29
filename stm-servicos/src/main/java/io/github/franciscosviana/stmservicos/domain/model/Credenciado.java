package io.github.franciscosviana.stmservicos.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Credenciado {

    @Id
    private UUID id;

    @Column(unique = true)
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
