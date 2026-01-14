package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.assembler.ContratoInputDisassembler;
import io.github.franciscosviana.stmservicos.api.assembler.ContratoOutputAssembler;
import io.github.franciscosviana.stmservicos.api.model.input.ContratoInput;
import io.github.franciscosviana.stmservicos.api.model.output.ContratoOutput;
import io.github.franciscosviana.stmservicos.common.validation.ClienteException;
import io.github.franciscosviana.stmservicos.common.validation.ContratoException;
import io.github.franciscosviana.stmservicos.domain.model.Cliente;
import io.github.franciscosviana.stmservicos.domain.model.Contrato;
import io.github.franciscosviana.stmservicos.domain.repository.ClienteRepository;
import io.github.franciscosviana.stmservicos.domain.repository.ContratoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class ContratoService {

    private final ClienteRepository clienteRepository;
    private final ContratoRepository contratoRepository;
    private final ContratoInputDisassembler contratoDisassembler;
    private final ContratoOutputAssembler contratoAssembler;

    public ContratoOutput adicionar(UUID clienteId, ContratoInput input) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ClienteException("Cliente não encontrado"));

        Contrato contrato = contratoDisassembler.toDomainObject(input);
        contrato.setCliente(cliente);

        contratoRepository.save(contrato);

        return contratoAssembler.toModel(contrato);
    }

    public List<ContratoOutput> listar(UUID clienteId) {
        return contratoRepository.findByClienteId(clienteId)
                .stream()
                .map(contratoAssembler::toModel)
                .toList();
    }

    public ContratoOutput atualizar(UUID clienteId, UUID contratoId, ContratoInput input) {
        Contrato contrato = contratoRepository
                .findByIdAndClienteId(contratoId, clienteId)
                .orElseThrow(() -> new ClienteException("Contrato não encontrado"));

        contratoDisassembler.copyToDomainObject(input, contrato);

        return contratoAssembler.toModel(contrato);
    }

    public void excluir(UUID clienteId, UUID contratoId) {
        Contrato contrato = contratoRepository
                .findByIdAndClienteId(contratoId, clienteId)
                .orElseThrow(() -> new ClienteException("Contrato não encontrado"));

        contratoRepository.delete(contrato);
    }

    public Contrato buscarOuFalhar(UUID contratoId) {
        return contratoRepository.findById(contratoId)
                .orElseThrow(() -> new ContratoException("Contrato não encontrado"));
    }
}

