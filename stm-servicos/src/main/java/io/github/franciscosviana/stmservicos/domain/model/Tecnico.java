package io.github.franciscosviana.stmservicos.domain.model;

import jakarta.persistence.*;
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
public class Tecnico {

    @Id
    private UUID id;

    @Column(unique = true)
    private Long codigo;

    private String nome;
    private String cpf;

    private String telefone;
    private String email;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credenciado_id", nullable = false)
    private Credenciado credenciado;

    @Embedded
    private Endereco endereco;
}
