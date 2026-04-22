package io.github.franciscosviana.stmservicos.api.model.input;

import io.github.franciscosviana.stmservicos.domain.model.enums.TipoPagamento;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class PagamentoLoteInput {

    private List<UUID> ordemServicoIds; // IDs das OS do lote

    // Dados de recebimento — aplicados a todas as OS
    private TipoPagamento tipoPagamento;
    private String lote;
    private String nf;
    private String banco;
    private String urlComprovante;
    private LocalDate dataPrevista;
    private LocalDate dataPagamento;
}