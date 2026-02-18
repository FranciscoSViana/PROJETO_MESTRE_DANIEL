package io.github.franciscosviana.stmservicos.api.assembler;

import io.github.franciscosviana.stmservicos.api.model.input.SolucaoOSInput;
import io.github.franciscosviana.stmservicos.domain.model.SolucaoOS;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SolucaoOSInputDisassembler {

    private final ModelMapper modelMapper;

    public SolucaoOS toDomainObject(SolucaoOSInput input) {
        return modelMapper.map(input, SolucaoOS.class);
    }

    public void copyToDomainObject(SolucaoOSInput input, SolucaoOS solucao) {
        modelMapper.map(input, solucao);
    }
}

