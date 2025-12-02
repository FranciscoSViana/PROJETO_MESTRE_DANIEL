package io.github.franciscosviana.stmservicos.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
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

    private String contrato;
    private String nome;

    private BigDecimal valorChamado;
    private BigDecimal valorKm;

    private String cnpj;
    private String inscricaoEstadual;
    private String razaoSocial;
}
