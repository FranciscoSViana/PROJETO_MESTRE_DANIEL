package io.github.franciscosviana.stmservicos.api.assembler;

import io.github.franciscosviana.stmservicos.api.model.output.SolucaoOSOutput;
import io.github.franciscosviana.stmservicos.domain.model.SolucaoOS;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SolucaoOSOutputAssembler {

    private final ModelMapper modelMapper;

    public SolucaoOSOutput toModel(SolucaoOS solucao) {

        SolucaoOSOutput output = modelMapper.map(solucao, SolucaoOSOutput.class);

        if (solucao.getOrdemServico() != null) {
            output.setOrdemServicoId(solucao.getOrdemServico().getId());
        }

        output.setOsClt(solucao.getOrdemServico().getOsClt());
        output.setOsg(solucao.getOrdemServico().getOsg());

        return output;
    }

    public List<SolucaoOSOutput> toCollectionModel(List<SolucaoOS> solucoes) {
        return solucoes.stream()
                .map(this::toModel)
                .toList();
    }
}

