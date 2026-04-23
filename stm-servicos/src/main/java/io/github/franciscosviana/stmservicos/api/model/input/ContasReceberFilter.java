package io.github.franciscosviana.stmservicos.api.model.input;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class ContasReceberFilter {

    private String osg;
    private String osClt;
    private String cliente;
    private String lote;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataAberturaInicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataAberturaFim;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataPagamentoInicio;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataPagamentoFim;

    private String clienteSnapshot; // ← novo campo para filtrar pelo snapshot

    private Boolean pago;
}