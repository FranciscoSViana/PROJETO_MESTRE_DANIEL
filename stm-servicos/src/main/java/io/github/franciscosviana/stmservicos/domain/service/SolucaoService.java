package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.assembler.SolucaoOSInputDisassembler;
import io.github.franciscosviana.stmservicos.api.assembler.SolucaoOSOutputAssembler;
import io.github.franciscosviana.stmservicos.api.model.input.SolucaoOSInput;
import io.github.franciscosviana.stmservicos.api.model.output.SolucaoOSOutput;
import io.github.franciscosviana.stmservicos.common.validation.OrdemServicoException;
import io.github.franciscosviana.stmservicos.domain.model.OrdemServico;
import io.github.franciscosviana.stmservicos.domain.model.SolucaoOS;
import io.github.franciscosviana.stmservicos.domain.model.enums.StatusOrdem;
import io.github.franciscosviana.stmservicos.domain.model.enums.TipoAcaoOS;
import io.github.franciscosviana.stmservicos.domain.repository.OrdemServicoRepository;
import io.github.franciscosviana.stmservicos.domain.repository.SolucaoOSRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SolucaoService {

    private final SolucaoOSRepository solucaoOSRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final SolucaoOSOutputAssembler solucaoOSOutputAssembler;
    private final SolucaoOSInputDisassembler solucaoOSInputDisassembler;
    private final HistoricoOrdemServicoService historicoOrdemServicoService;

    @Transactional
    public SolucaoOSOutput finalizarOS(UUID ordemId, SolucaoOSInput input) {

        OrdemServico ordem = ordemServicoRepository.findById(ordemId)
                .orElseThrow(() -> new OrdemServicoException("OS não encontrada"));

        if (ordem.getStatus() == StatusOrdem.CONCLUIDA) {
            throw new OrdemServicoException("Ordem já está concluída.");
        }

        SolucaoOS solucao = solucaoOSInputDisassembler.toDomainObject(input);
        solucao.setOrdemServico(ordem);

        ordem.concluirCom(solucao);

        SolucaoOS salva = solucaoOSRepository.save(solucao);

        // 🔥 REGISTRA HISTÓRICO AQUI
        historicoOrdemServicoService.registrar(
                ordem,
                TipoAcaoOS.CONCLUSAO_ORDEM,
                montarDescricaoConclusao(solucao)
        );

        return solucaoOSOutputAssembler.toModel(salva);
    }

    public Page<SolucaoOSOutput> listarTodas(Pageable pageable) {

        Page<SolucaoOS> page = solucaoOSRepository.findAll(pageable);

        return page.map(solucaoOSOutputAssembler::toModel);
    }

    public SolucaoOSOutput buscarPorId(Long id) {

        SolucaoOS solucao = solucaoOSRepository.findById(id)
                .orElseThrow(() -> new OrdemServicoException("Solução não encontrada"));

        return solucaoOSOutputAssembler.toModel(solucao);
    }

    public SolucaoOSOutput buscarPorOrdem(UUID ordemId) {

        SolucaoOS solucao = solucaoOSRepository
                .findByOrdemServicoId(ordemId)
                .orElseThrow(() -> new OrdemServicoException("Solução não encontrada para a OS"));

        return solucaoOSOutputAssembler.toModel(solucao);
    }

    private String montarDescricaoConclusao(SolucaoOS solucao) {

        return """
                Ordem concluída.
                
                Solução: %s
                
                Início: %s
                Fim: %s
                
                KM: %s
                Pedágio: %s
                Estacionamento: %s
                Outros: %s
                """.formatted(
                solucao.getSolucao(),
                solucao.getHoraInicial(),
                solucao.getHoraFinal(),
                solucao.getKm(),
                solucao.getPedagio(),
                solucao.getEstacionamento(),
                solucao.getOutros()
        );
    }
}
