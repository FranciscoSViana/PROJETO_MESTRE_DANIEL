package io.github.franciscosviana.stmservicos.api.model.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContratoOutput {

    private UUID id;
    private String numeroContrato;
    private String nomeContrato;
    private BigDecimal valorChamado;
    private BigDecimal valorKm;
    private String responsavelContrato;
    private String telefoneContrato;
}
