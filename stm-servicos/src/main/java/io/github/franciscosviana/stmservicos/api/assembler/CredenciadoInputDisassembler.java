package io.github.franciscosviana.stmservicos.api.assembler;

import io.github.franciscosviana.stmservicos.api.model.input.CredenciadoInput;
import io.github.franciscosviana.stmservicos.domain.model.Credenciado;
import io.github.franciscosviana.stmservicos.domain.model.enums.TipoPessoa;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CredenciadoInputDisassembler {

    private final ModelMapper modelMapper;

    public Credenciado toDomainObject(CredenciadoInput credenciadoInput) {
        Credenciado credenciado = modelMapper.map(credenciadoInput, Credenciado.class);

        credenciado.setTipoPessoa(TipoPessoa.fromCodigo(credenciadoInput.getTipoPessoa()));

        return credenciado;
    }

    public void copyToDomainObject(CredenciadoInput credenciadoInput, Credenciado credenciado) {
        modelMapper.map(credenciadoInput, credenciado);

        credenciado.setTipoPessoa(TipoPessoa.fromCodigo(credenciadoInput.getTipoPessoa()));
    }
}
