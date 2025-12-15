package io.github.franciscosviana.stmservicos.api.assembler;

import io.github.franciscosviana.stmservicos.api.model.output.ContratoOutput;
import io.github.franciscosviana.stmservicos.domain.model.Contrato;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ContratoOutputAssembler {

    private final ModelMapper modelMapper;

    public ContratoOutput toModel(Contrato contrato) {
        return modelMapper.map(contrato, ContratoOutput.class);
    }

    public List<ContratoOutput> toCollectionModel(List<Contrato> contratos) {
        return contratos.stream()
                .map(this::toModel)
                .toList();
    }
}
