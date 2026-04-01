package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.assembler.SolucaoOSInputDisassembler;
import io.github.franciscosviana.stmservicos.api.assembler.SolucaoOSOutputAssembler;
import io.github.franciscosviana.stmservicos.api.model.input.SolucaoOSInput;
import io.github.franciscosviana.stmservicos.api.model.output.SolucaoOSOutput;
import io.github.franciscosviana.stmservicos.common.validation.OrdemServicoException;
import io.github.franciscosviana.stmservicos.domain.model.OrdemServico;
import io.github.franciscosviana.stmservicos.domain.model.PagamentoOS;
import io.github.franciscosviana.stmservicos.domain.model.SolucaoOS;
import io.github.franciscosviana.stmservicos.domain.model.enums.StatusOrdem;
import io.github.franciscosviana.stmservicos.domain.model.enums.TipoAcaoOS;
import io.github.franciscosviana.stmservicos.domain.repository.OrdemServicoRepository;
import io.github.franciscosviana.stmservicos.domain.repository.PagamentoOSRepository;
import io.github.franciscosviana.stmservicos.domain.repository.SolucaoOSRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SolucaoService {

    private final SolucaoOSRepository solucaoOSRepository;
    private final PagamentoOSRepository pagamentoOSRepository;
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

        // Cria rascunho de pagamento (pago = false) com valores base do credenciado
        criarRascunhoPagamento(ordem, salva);

        historicoOrdemServicoService.registrar(
                ordem,
                TipoAcaoOS.CONCLUSAO_ORDEM,
                montarDescricaoConclusao(solucao)
        );

        return solucaoOSOutputAssembler.toModel(salva);
    }

    /**
     * Cria um PagamentoOS com pago=false ao finalizar a OS.
     * Usa os valores padrão do credenciado como base e os custos da solução.
     * O financeiro depois preenche os dados de pagamento via PUT.
     */
    private void criarRascunhoPagamento(OrdemServico ordem, SolucaoOS solucao) {

        // Evita duplicata caso finalizarOS seja chamado por alguma reentrada
        if (pagamentoOSRepository.findByOrdemServicoId(ordem.getId()).isPresent()) {
            log.warn("[SolucaoService] PagamentoOS já existe para OS={}, rascunho ignorado.", ordem.getOsg());
            return;
        }

        BigDecimal valorChamado = nvl(ordem.getCredenciado() != null ? ordem.getCredenciado().getValorChamado() : null);
        BigDecimal valorKm = nvl(ordem.getCredenciado() != null ? ordem.getCredenciado().getValorKm() : null);
        BigDecimal km = nvl(solucao.getKm());
        BigDecimal pedagio = nvl(solucao.getPedagio());
        BigDecimal estacionamento = nvl(solucao.getEstacionamento());
        BigDecimal valorOutros = solucao.getOutros() != null
                ? parseBD(solucao.getOutros().toPlainString()) : BigDecimal.ZERO;

        BigDecimal valorTotal = valorChamado
                .add(km.multiply(valorKm))
                .add(pedagio)
                .add(estacionamento)
                .add(valorOutros);

        PagamentoOS rascunho = PagamentoOS.builder()
                .ordemServico(ordem)
                .osClt(ordem.getOsClt())
                .osg(ordem.getOsg())
                .cliente(ordem.getCliente() != null ? ordem.getCliente().getNome() : null)
                .contrato(ordem.getContrato() != null ? String.valueOf(ordem.getContrato().getId()) : null)
                .valorChamado(valorChamado)
                .km(km)
                .valorKm(valorKm)
                .pedagio(pedagio)
                .estacionamento(estacionamento)
                .outros(solucao.getOutros() != null ? solucao.getOutros().toPlainString() : null)
                .valorOutros(valorOutros)
                .valorTotal(valorTotal)
                // campos de pagamento ficam nulos até o financeiro preencher
                .tipoPagamento(null)   // ← nullable: ajuste a constraint no model se necessário
                .pago(false)
                .build();

        pagamentoOSRepository.save(rascunho);

        log.info("[SolucaoService] Rascunho de pagamento criado para OS={} | total={}", ordem.getOsg(), valorTotal);
    }

    // ── Demais métodos ────────────────────────────────────────────────────────

    public Page<SolucaoOSOutput> listarTodas(Pageable pageable) {
        return solucaoOSRepository.findAll(pageable)
                .map(solucaoOSOutputAssembler::toModel);
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

    private BigDecimal nvl(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private BigDecimal parseBD(String s) {
        try {
            return new BigDecimal(s);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}