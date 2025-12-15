package io.github.franciscosviana.stmservicos.api.assembler;

import io.github.franciscosviana.stmservicos.api.model.input.ContratoInput;
import io.github.franciscosviana.stmservicos.domain.model.Contrato;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContratoInputDisassembler {

    private final ModelMapper modelMapper;

    public Contrato toDomainObject(ContratoInput contratoInput) {
        return modelMapper.map(contratoInput, Contrato.class);
    }

    public void copyToDomainObject(ContratoInput contratoInput, Contrato contrato) {
        modelMapper.map(contratoInput, contrato);
    }
}
