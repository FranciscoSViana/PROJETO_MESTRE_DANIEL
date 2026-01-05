package io.github.franciscosviana.stmservicos.domain.model;

import io.github.franciscosviana.stmservicos.api.model.input.SolucaoOSInput;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "solucao_os")
public class SolucaoOS {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "ordem_servico_id")
    private OrdemServico ordemServico;

    private String tecnico;

    private OffsetDateTime dataVisita;

    private OffsetDateTime inicio;

    private OffsetDateTime termino;

    private String solucao;

    private BigDecimal km;
    private BigDecimal pedagios;
    private BigDecimal estac;
    private String outros;

    public static SolucaoOS from(SolucaoOSInput input) {
        if (input == null) return null;

        return SolucaoOS.builder()
                .tecnico(input.getTecnico())
                .dataVisita(input.getDataVisita())
                .inicio(input.getInicio())
                .termino(input.getTermino())
                .solucao(input.getSolucao())
                .km(input.getKm())
                .pedagios(input.getPedagios())
                .estac(input.getEstac())
                .outros(input.getOutros())
                .build();
    }
}
