package io.github.franciscosviana.stmservicos.api.assembler;

import io.github.franciscosviana.stmservicos.api.model.input.ClienteInput;
import io.github.franciscosviana.stmservicos.domain.model.Cliente;
import io.github.franciscosviana.stmservicos.domain.model.Contrato;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClienteInputDisassembler {

    private final ModelMapper modelMapper;
    private final ContratoInputDisassembler contratoDisassembler;

    public Cliente toDomainObject(ClienteInput input) {
        Cliente cliente = modelMapper.map(input, Cliente.class);

        if (input.getContratos() != null) {
            input.getContratos().forEach(c -> {
                Contrato contrato = contratoDisassembler.toDomainObject(c);
                contrato.setCliente(cliente);
                cliente.getContratos().add(contrato);
            });
        }

        return cliente;
    }

    public void copyToDomainObject(ClienteInput input, Cliente cliente) {

        modelMapper.typeMap(ClienteInput.class, Cliente.class)
                .addMappings(mapper -> mapper.skip(Cliente::setContratos));

        modelMapper.map(input, cliente);
    }
}
