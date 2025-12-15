package io.github.franciscosviana.stmservicos.api.assembler;

import io.github.franciscosviana.stmservicos.api.model.input.TecnicoInput;
import io.github.franciscosviana.stmservicos.domain.model.Tecnico;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TecnicoInputDisassembler {

    private final ModelMapper modelMapper;

    public Tecnico toDomainObject(TecnicoInput tecnicoInput) {
        return modelMapper.map(tecnicoInput, Tecnico.class);
    }

    public void copyToDomainObject(TecnicoInput tecnicoInput, Tecnico tecnico) {
        modelMapper.map(tecnicoInput, tecnico);
    }
}
