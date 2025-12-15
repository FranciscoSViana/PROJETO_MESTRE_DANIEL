package io.github.franciscosviana.stmservicos.api.assembler;

import io.github.franciscosviana.stmservicos.api.model.output.TecnicoOutput;
import io.github.franciscosviana.stmservicos.domain.model.Tecnico;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TecnicoOutputAssembler {

    private final ModelMapper modelMapper;

    public TecnicoOutput toModel(Tecnico tecnico) {
        return modelMapper.map(tecnico, TecnicoOutput.class);
    }

    public List<TecnicoOutput> toCollectionModel(List<Tecnico> tecnicos) {
        return tecnicos.stream()
                .map(this::toModel)
                .toList();
    }
}
