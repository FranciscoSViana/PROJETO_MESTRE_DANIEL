package io.github.franciscosviana.stmservicos.api.assembler;

import io.github.franciscosviana.stmservicos.api.model.output.HistoricoOrdemServicoOutput;
import io.github.franciscosviana.stmservicos.api.model.output.UsuarioResumoOutput;
import io.github.franciscosviana.stmservicos.domain.model.HistoricoOrdemServico;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HistoricoOrdemServicoOutputAssembler {

    private final ModelMapper modelMapper;

    public HistoricoOrdemServicoOutput toModel(HistoricoOrdemServico historico) {

        if (historico == null) return null;

        HistoricoOrdemServicoOutput output =
                modelMapper.map(historico, HistoricoOrdemServicoOutput.class);

        output.setOrdemServicoId(historico.getOrdemServico().getId());
        output.setAcao(historico.getAcao().name());

        output.setUsuario(
                modelMapper.map(
                        historico.getUsuario(),
                        UsuarioResumoOutput.class
                )
        );

        return output;
    }

    public List<HistoricoOrdemServicoOutput> toCollectionModel(List<HistoricoOrdemServico> historicos) {
        return historicos.stream()
                .map(this::toModel)
                .toList();
    }
}

