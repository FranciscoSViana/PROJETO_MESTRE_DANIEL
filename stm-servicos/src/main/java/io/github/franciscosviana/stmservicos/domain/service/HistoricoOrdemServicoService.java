package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.assembler.HistoricoOrdemServicoOutputAssembler;
import io.github.franciscosviana.stmservicos.api.model.output.HistoricoOrdemServicoOutput;
import io.github.franciscosviana.stmservicos.domain.model.HistoricoOrdemServico;
import io.github.franciscosviana.stmservicos.domain.model.OrdemServico;
import io.github.franciscosviana.stmservicos.domain.model.enums.TipoAcaoOS;
import io.github.franciscosviana.stmservicos.domain.repository.HistoricoOrdemServicoRepository;
import io.github.franciscosviana.stmservicos.domain.service.helper.UsuarioAutenticadoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HistoricoOrdemServicoService {

    private final HistoricoOrdemServicoRepository repository;
    private final UsuarioAutenticadoService usuarioAutenticadoService;
    private final HistoricoOrdemServicoOutputAssembler assembler;

    public void registrar(
            OrdemServico os,
            TipoAcaoOS acao,
            String descricao
    ) {
        HistoricoOrdemServico historico = HistoricoOrdemServico.builder()
                .ordemServico(os)
                .usuario(usuarioAutenticadoService.getUsuarioLogado())
                .acao(acao)
                .descricao(descricao)
                .dataHora(OffsetDateTime.now())
                .build();

        repository.save(historico);
    }

    @Transactional
    public void deletarPorOrdemServico(UUID ordemServicoId) {
        List<HistoricoOrdemServico> historicos = repository.findByOrdemServicoIdOrderByDataHoraAsc(ordemServicoId);
        repository.deleteAll(historicos);
    }

    public List<HistoricoOrdemServicoOutput> listarPorOrdemServico(UUID ordemServicoId) {

        return repository
                .findByOrdemServicoIdOrderByDataHoraAsc(ordemServicoId)
                .stream()
                .map(assembler::toModel)
                .toList();
    }
}