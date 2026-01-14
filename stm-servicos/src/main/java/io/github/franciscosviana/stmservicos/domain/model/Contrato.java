package io.github.franciscosviana.stmservicos.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contrato {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String numeroContrato;

    @Column(nullable = false)
    private String nomeContrato;

    private BigDecimal valorChamado;
    private BigDecimal valorKm;

    @Column(nullable = false)
    private String responsavelContrato;

    @Column(nullable = false)
    private String telefoneContrato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;
}
