package io.github.franciscosviana.stmservicos.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolucaoOS {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private OrdemServico ordemServico;

    private String tecnico;
    private OffsetDateTime dataVisita;

    private OffsetDateTime inicio;
    private OffsetDateTime termino;

    private String solucao;

    private double km;
    private double pedagios;
    private double estac;
    private String outros;
}
