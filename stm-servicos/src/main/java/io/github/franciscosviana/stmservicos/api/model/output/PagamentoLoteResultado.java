package io.github.franciscosviana.stmservicos.api.model.output;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class PagamentoLoteResultado {
    private int totalProcessado;
    private int totalSucesso;
    private int totalErro;
    private List<String> erros;
}