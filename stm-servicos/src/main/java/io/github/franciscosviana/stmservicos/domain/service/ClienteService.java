package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.assembler.ClienteInputDisassembler;
import io.github.franciscosviana.stmservicos.api.assembler.ClienteOutputAssembler;
import io.github.franciscosviana.stmservicos.api.assembler.ContratoInputDisassembler;
import io.github.franciscosviana.stmservicos.api.model.input.ClienteInput;
import io.github.franciscosviana.stmservicos.api.model.input.ContratoInput;
import io.github.franciscosviana.stmservicos.api.model.output.ClienteOutput;
import io.github.franciscosviana.stmservicos.common.client.BrasilAPIClient;
import io.github.franciscosviana.stmservicos.common.client.model.ReceitaWsResponse;
import io.github.franciscosviana.stmservicos.common.validation.ClienteException;
import io.github.franciscosviana.stmservicos.domain.model.Cliente;
import io.github.franciscosviana.stmservicos.domain.model.Contrato;
import io.github.franciscosviana.stmservicos.domain.repository.ClienteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final BrasilAPIClient receitaWsClient;
    private final ClienteRepository clienteRepository;
    private final ClienteOutputAssembler clienteOutputAssembler;
    private final ClienteInputDisassembler clienteInputDisassembler;
    private final ContratoInputDisassembler contratoInputDisassembler;

    @Transactional
    public ClienteOutput salvar(ClienteInput clienteInput) {

        Long ultimo = clienteRepository.buscarUltimoCodigo();
        Long proximoCodigo = ultimo + 1;

        var dadosCnpj = receitaWsClient.consultarCnpj(clienteInput.getCnpj());

        Cliente cliente = clienteInputDisassembler.toDomainObject(clienteInput);
        cliente.setCodigo(proximoCodigo);
        cliente.setRazaoSocial(dadosCnpj.getRazaoSocial());

        clienteRepository.save(cliente);

        return clienteOutputAssembler.toModel(cliente);
    }

    public ClienteOutput buscarPorId(UUID id) {

        Cliente cliente = buscarOuFalhar(id);

        return clienteOutputAssembler.toModel(cliente);
    }


    public Page<ClienteOutput> listar(Pageable pageable) {
        return clienteRepository.findAll(pageable)
                .map(clienteOutputAssembler::toModel);
    }

    @Transactional
    public ClienteOutput atualizar(UUID id, ClienteInput clienteInput) {

        Cliente cliente = buscarOuFalhar(id);

        clienteInputDisassembler.copyToDomainObject(clienteInput, cliente);

        clienteRepository.save(cliente);

        return clienteOutputAssembler.toModel(cliente);
    }

    @Transactional
    public void excluir(UUID id) {

        Cliente cliente = buscarOuFalhar(id);

        clienteRepository.delete(cliente);
    }

    @Transactional
    public ClienteOutput adicionarContratos(
            UUID clienteId,
            ContratoInput contratoInput) {

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        Contrato contrato = contratoInputDisassembler.toDomainObject(contratoInput);
        contrato.setId(UUID.randomUUID());
        contrato.setCliente(cliente);

        if (cliente.getContratos() == null) {
            cliente.setContratos(new ArrayList<>());
        }

        cliente.getContratos().add(contrato);

        clienteRepository.save(cliente);

        return clienteOutputAssembler.toModel(cliente);
    }


    public ClienteOutput buscarPorCodigo(Long codigo) {
        Cliente cliente = clienteRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ClienteException("Cliente não encontrado pelo código: " + codigo));

        return clienteOutputAssembler.toModel(cliente);
    }

    public ReceitaWsResponse consultarCnpj(String cnpj) {
        return receitaWsClient.consultarCnpj(cnpj);
    }

    public Cliente buscarOuFalhar(UUID id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ClienteException("Cliente não encontrado"));
    }
}
