package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.assembler.OrdemServicoInputDisassembler;
import io.github.franciscosviana.stmservicos.api.assembler.OrdemServicoOutputAssembler;
import io.github.franciscosviana.stmservicos.api.model.input.OrdemServicoInput;
import io.github.franciscosviana.stmservicos.api.model.output.OrdemServicoOutput;
import io.github.franciscosviana.stmservicos.common.validation.ContratoException;
import io.github.franciscosviana.stmservicos.common.validation.OrdemServicoException;
import io.github.franciscosviana.stmservicos.domain.model.Cliente;
import io.github.franciscosviana.stmservicos.domain.model.Contrato;
import io.github.franciscosviana.stmservicos.domain.model.OrdemServico;
import io.github.franciscosviana.stmservicos.domain.model.Tecnico;
import io.github.franciscosviana.stmservicos.domain.repository.OrdemServicoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrdemServicoService {

    private final TecnicoService tecnicoService;
    private final ClienteService clienteService;
    private final ContratoService contratoService;
    private final OrdemServicoRepository repository;
    private final OrdemServicoOutputAssembler assembler;
    private final OrdemServicoInputDisassembler disassembler;

    @Transactional
    public OrdemServicoOutput salvar(OrdemServicoInput input) {

        OrdemServico ordem = disassembler.toDomainObject(input);
        ordem.setId(UUID.randomUUID());

        ordem.setOsg(gerarProximoOsg());

        // Cliente
        var cliente = clienteService.buscarOuFalhar(input.getClienteId());

        // Contrato
        if (input.getContratoId() == null) {
            throw new ContratoException("Contrato não informado.");
        }
        var contrato = contratoService.buscarOuFalhar(input.getContratoId());

        if (!contrato.getCliente().getId().equals(cliente.getId())) {
            throw new ContratoException("Contrato não pertence ao cliente");
        }

        // 🔥 Técnico (OBRIGATÓRIO)
        if (input.getTecnicoId() == null) {
            throw new OrdemServicoException("Técnico é obrigatório para criação da OS");
        }

        Tecnico tecnico = tecnicoService.buscarOuFalhar(input.getTecnicoId());

        // 🔐 Integridade: técnico pertence ao credenciado?
        if (!tecnico.getCredenciado().getId().equals(input.getCredenciadoId())) {
            throw new OrdemServicoException("Técnico não pertence ao credenciado informado");
        }

        // Set entidades gerenciadas
        ordem.setCliente(cliente);
        ordem.setContrato(contrato);
        ordem.setTecnico(tecnico);

        repository.save(ordem);
        return assembler.toModel(ordem);
    }


    public OrdemServicoOutput buscarPorId(UUID id) {

        OrdemServico ordemServico = repository.buscarCompleta(id)
                .orElseThrow(() -> new OrdemServicoException("Ordem de Serviço não encontrada"));

        return assembler.toModel(ordemServico);
    }

    public Page<OrdemServicoOutput> listar(Pageable pageable) {
        return repository.findAll(pageable)
                .map(assembler::toModel);
    }

    public OrdemServicoOutput atualizar(UUID id, OrdemServicoInput input) {
        OrdemServico atual = buscarOuFalhar(id);

        disassembler.copyToDomainObject(input, atual);

        Cliente cliente = clienteService.buscarOuFalhar(atual.getCliente().getId());

        if (atual.getContrato() == null || atual.getContrato().getId() == null) {
            throw new ContratoException("Contrato não informado");
        }

        Contrato contrato = contratoService.buscarOuFalhar(atual.getContrato().getId());


        if (!contrato.getCliente().getId().equals(cliente.getId())) {
            throw new ContratoException("Contrato não pertence ao cliente");
        }

        atual.setCliente(cliente);
        atual.setContrato(contrato);
        repository.save(atual);
        return assembler.toModel(atual);
    }

    public void deletar(UUID id) {

        OrdemServico ordemServico = buscarOuFalhar(id);

        repository.delete(ordemServico);
    }

    public String gerarProximoOsg() {

        String anoAtual = String.format("%02d", OffsetDateTime.now().getYear() % 100);

        List<String> result = repository.findLastOsg(PageRequest.of(0, 1));

        if (result.isEmpty()) {
            return "OSG" + anoAtual + "0001";
        }

        String ultimoOsg = result.get(0);

        String anoUltimo = ultimoOsg.substring(3, 5);
        int sequencia = Integer.parseInt(ultimoOsg.substring(5));

        if (!anoUltimo.equals(anoAtual)) {
            return "OSG" + anoAtual + "0001";
        }

        sequencia++;

        return "OSG" + anoAtual + String.format("%04d", sequencia);
    }

    private OrdemServico buscarOuFalhar(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new OrdemServicoException("Ordem de Serviço não encontrada"));
    }
}
