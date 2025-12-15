package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.assembler.OrdemServicoInputDisassembler;
import io.github.franciscosviana.stmservicos.api.assembler.OrdemServicoOutputAssembler;
import io.github.franciscosviana.stmservicos.api.model.input.OrdemServicoInput;
import io.github.franciscosviana.stmservicos.api.model.output.OrdemServicoOutput;
import io.github.franciscosviana.stmservicos.common.validation.OrdemServicoException;
import io.github.franciscosviana.stmservicos.domain.model.OrdemServico;
import io.github.franciscosviana.stmservicos.domain.repository.OrdemServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrdemServicoService {

    private final OrdemServicoRepository repository;
    private final OrdemServicoOutputAssembler assembler;
    private final OrdemServicoInputDisassembler disassembler;

    public OrdemServicoOutput salvar(OrdemServicoInput input) {
        OrdemServico ordem = disassembler.toDomainObject(input);

        ordem.setId(UUID.randomUUID());

        repository.save(ordem);
        return assembler.toModel(ordem);
    }

    public OrdemServicoOutput buscarPorId(UUID id) {

        OrdemServico ordemServico = buscarOuFalhar(id);

        return assembler.toModel(ordemServico);
    }

    public Page<OrdemServicoOutput> listar(Pageable pageable) {
        return repository.findAll(pageable)
                .map(assembler::toModel);
    }

    public OrdemServicoOutput atualizar(UUID id, OrdemServicoInput input) {
        OrdemServico atual = buscarOuFalhar(id);
        disassembler.copyToDomainObject(input, atual);
        repository.save(atual);
        return assembler.toModel(atual);
    }

    public void deletar(UUID id) {

        OrdemServico ordemServico = buscarOuFalhar(id);

        repository.delete(ordemServico);
    }

    public String gerarProximoOsg() {
        String anoAtual = String.valueOf(OffsetDateTime.now().getYear() % 100);
        anoAtual = String.format("%02d", Integer.parseInt(anoAtual));
        String ultimoOsg = repository.findLastOsg();

        if (ultimoOsg == null) {
            return "OSG" + anoAtual + "0001";
        }

        String anoUltimo = ultimoOsg.substring(3, 5);
        int sequencia = Integer.parseInt(ultimoOsg.substring(5));

        if (!anoUltimo.equals(anoAtual)) {
            return "OSG" + anoAtual + "0001";
        }

        sequencia++;
        String seq = sequencia <= 9999
                ? String.format("%04d", sequencia)
                : String.valueOf(sequencia);

        return "OSG" + anoAtual + seq;
    }

    private OrdemServico buscarOuFalhar(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new OrdemServicoException("Ordem de Serviço não encontrada"));
    }
}
