package io.github.franciscosviana.stmservicos.api.model.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolucaoOSInput {

    private String tecnico;
    private OffsetDateTime dataVisita;
    private OffsetDateTime inicio;
    private OffsetDateTime termino;
    private String solucao;
    private BigDecimal km;
    private BigDecimal pedagios;
    private BigDecimal estac;
    private String outros;
}
