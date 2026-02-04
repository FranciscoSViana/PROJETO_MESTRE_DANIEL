package io.github.franciscosviana.stmservicos.api.model.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoOrdemServicoOutput {

    private UUID id;

    private UUID ordemServicoId;

    private String acao;

    private String descricao;

    private OffsetDateTime dataHora;

    private UsuarioResumoOutput usuario;
}
