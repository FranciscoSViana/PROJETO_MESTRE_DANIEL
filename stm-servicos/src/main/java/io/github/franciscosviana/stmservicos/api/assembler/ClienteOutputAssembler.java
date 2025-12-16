package io.github.franciscosviana.stmservicos.api.assembler;

import io.github.franciscosviana.stmservicos.api.model.output.ClienteOutput;
import io.github.franciscosviana.stmservicos.api.model.output.EnderecoOutput;
import io.github.franciscosviana.stmservicos.domain.model.Cliente;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ClienteOutputAssembler {

    private final ContratoOutputAssembler contratoAssembler;

    public ClienteOutput toModel(Cliente cliente) {
        return ClienteOutput.builder()
                .id(cliente.getId())
                .codigo(cliente.getCodigo())
                .nome(cliente.getNome())
                .valorChamado(cliente.getValorChamado())
                .valorKm(cliente.getValorKm())
                .cnpj(cliente.getCnpj())
                .inscricaoEstadual(cliente.getInscricaoEstadual())
                .razaoSocial(cliente.getRazaoSocial())
                .endereco(EnderecoOutput.from(cliente.getEndereco()))
                .contratos(
                        cliente.getContratos() == null
                                ? List.of()
                                : cliente.getContratos().stream()
                                .map(contratoAssembler::toModel)
                                .toList()
                )
                .build();
    }

    public List<ClienteOutput> toCollectionModel(List<Cliente> clientes) {
        return clientes.stream()
                .map(this::toModel)
                .toList();
    }
}
