package io.github.franciscosviana.stmservicos.api.model.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContratoInput {

    private String numeroContrato;
    private String nomeContrato;
    private BigDecimal valorChamado;
    private BigDecimal valorKm;
    private String responsavelContrato;
    private String telefoneContrato;
}
