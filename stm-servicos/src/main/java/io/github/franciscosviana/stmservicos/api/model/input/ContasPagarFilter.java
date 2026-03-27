package io.github.franciscosviana.stmservicos.api.model.input;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class ContasPagarFilter {

    private String osg;
    private String osClt;
    private String cliente;
    private String credenciado;
    private Boolean pago;
    private String lote;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataAberturaInicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataAberturaFim;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataPagamentoInicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataPagamentoFim;
}