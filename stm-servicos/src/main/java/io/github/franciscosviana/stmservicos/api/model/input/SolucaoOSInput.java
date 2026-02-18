package io.github.franciscosviana.stmservicos.api.model.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SolucaoOSInput {

    private OffsetDateTime dataAtendimento;
    private OffsetDateTime horaInicial;
    private OffsetDateTime horaFinal;
    private String solucao;
    private String pecaSolicitada;
    private String observacao;
    private BigDecimal km;
    private BigDecimal pedagio;
    private BigDecimal estacionamento;
    private BigDecimal outros;
}
