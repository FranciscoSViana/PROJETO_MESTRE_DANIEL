package io.github.franciscosviana.stmservicos.api.assembler;

import io.github.franciscosviana.stmservicos.api.model.output.CredenciadoOutput;
import io.github.franciscosviana.stmservicos.domain.model.Credenciado;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CredenciadoOutputAssembler {

    private final ModelMapper modelMapper;

    public CredenciadoOutput toModel(Credenciado credenciado) {
        return modelMapper.map(credenciado, CredenciadoOutput.class);
    }

    public List<CredenciadoOutput> toCollectionModel(List<Credenciado> credenciados) {
        return credenciados.stream()
                .map(this::toModel)
                .toList();
    }
}
