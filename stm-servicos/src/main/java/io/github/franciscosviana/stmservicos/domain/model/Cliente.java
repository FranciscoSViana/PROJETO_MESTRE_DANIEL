package io.github.franciscosviana.stmservicos.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    private UUID id;

    @Column(unique = true)
    private Long codigo;

    private String nome;

    @Builder.Default
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Contrato> contratos = new ArrayList<>();

    private BigDecimal valorChamado;
    private BigDecimal valorKm;

    private String cnpj;
    private String inscricaoEstadual;
    private String razaoSocial;

    @Embedded
    private Endereco endereco;

    @PrePersist
    public void gerarId() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}
