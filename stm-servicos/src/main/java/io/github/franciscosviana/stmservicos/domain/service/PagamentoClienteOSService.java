package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.assembler.PagamentoClienteOSOutputAssembler;
import io.github.franciscosviana.stmservicos.api.model.input.PagamentoClienteOSInput;
import io.github.franciscosviana.stmservicos.api.model.output.PagamentoClienteOSOutput;
import io.github.franciscosviana.stmservicos.common.validation.OrdemServicoException;
import io.github.franciscosviana.stmservicos.domain.model.OrdemServico;
import io.github.franciscosviana.stmservicos.domain.model.PagamentoClienteOS;
import io.github.franciscosviana.stmservicos.domain.model.SolucaoOS;
import io.github.franciscosviana.stmservicos.domain.repository.OrdemServicoRepository;
import io.github.franciscosviana.stmservicos.domain.repository.PagamentoClienteOSRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagamentoClienteOSService {

    private final PagamentoClienteOSRepository pagamentoClienteOSRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final PagamentoClienteOSOutputAssembler assembler;

    @Transactional
    public PagamentoClienteOSOutput registrar(UUID ordemServicoId, PagamentoClienteOSInput input) {

        OrdemServico os = ordemServicoRepository.buscarCompleta(ordemServicoId)
                .orElseThrow(() -> new OrdemServicoException("Ordem de Serviço não encontrada"));

        SolucaoOS solucao = os.getSolucao();
        if (solucao == null) {
            throw new OrdemServicoException("A OS ainda não possui solução registrada.");
        }

        PagamentoClienteOS pagamento = pagamentoClienteOSRepository
                .findByOrdemServicoId(ordemServicoId)
                .orElse(new PagamentoClienteOS());

        if (pagamento.isRecebido()) {
            throw new OrdemServicoException(
                    "Este recebimento já foi registrado. Use o endpoint de edição para alterá-lo.");
        }

        preencherPagamento(pagamento, os, solucao, input);
        pagamento.setRecebido(true);

        pagamentoClienteOSRepository.save(pagamento);
        return assembler.toModel(pagamento);
    }

    @Transactional
    public PagamentoClienteOSOutput editar(UUID ordemServicoId, PagamentoClienteOSInput input) {

        OrdemServico os = ordemServicoRepository.buscarCompleta(ordemServicoId)
                .orElseThrow(() -> new OrdemServicoException("Ordem de Serviço não encontrada"));

        SolucaoOS solucao = os.getSolucao();
        if (solucao == null) {
            throw new OrdemServicoException("A OS ainda não possui solução registrada.");
        }

        PagamentoClienteOS pagamento = pagamentoClienteOSRepository
                .findByOrdemServicoId(ordemServicoId)
                .orElseThrow(() -> new OrdemServicoException(
                        "Nenhum recebimento encontrado para edição. Registre o recebimento primeiro."));

        preencherPagamento(pagamento, os, solucao, input);

        if (input.getRecebido() != null) {
            pagamento.setRecebido(input.getRecebido());
        }

        pagamentoClienteOSRepository.save(pagamento);
        return assembler.toModel(pagamento);
    }

    public PagamentoClienteOSOutput buscarPorOrdemServico(UUID ordemServicoId) {
        PagamentoClienteOS pagamento = pagamentoClienteOSRepository
                .findByOrdemServicoId(ordemServicoId)
                .orElseThrow(() -> new OrdemServicoException(
                        "Recebimento não encontrado para a OS informada"));
        return assembler.toModel(pagamento);
    }

    public Optional<PagamentoClienteOSOutput> buscarPorOrdemServicoOpcional(UUID ordemServicoId) {
        return pagamentoClienteOSRepository
                .findByOrdemServicoId(ordemServicoId)
                .map(assembler::toModel);
    }

    // -------------------------------------------------------------------------

    private void preencherPagamento(PagamentoClienteOS pagamento, OrdemServico os,
                                    SolucaoOS solucao, PagamentoClienteOSInput input) {

        BigDecimal valorChamado = resolverValor(
                input.getValorChamado(),
                os.getCliente() != null ? os.getCliente().getValorChamado() : null,
                "valorChamado", os.getOsg()
        );
        BigDecimal valorKm = resolverValor(
                input.getValorKm(),
                os.getCliente() != null ? os.getCliente().getValorKm() : null,
                "valorKm", os.getOsg()
        );

        BigDecimal km             = nvl(input.getKm());
        BigDecimal pedagio        = nvl(input.getPedagio());
        BigDecimal estacionamento = nvl(input.getEstacionamento());
        BigDecimal valorOutros    = nvl(input.getValorOutros());

        BigDecimal totalKm    = km.multiply(valorKm);
        BigDecimal valorTotal = valorChamado.add(totalKm).add(pedagio).add(estacionamento).add(valorOutros);

        log.info("[PagamentoClienteOS] OS={} | chamado={} | km={}x{}={} | pedágio={} | estac={} | outros={} | TOTAL={}",
                os.getOsg(), valorChamado, km, valorKm, totalKm, pedagio, estacionamento, valorOutros, valorTotal);

        // Snapshot
        pagamento.setOrdemServico(os);
        pagamento.setOsClt(os.getOsClt());
        pagamento.setOsg(os.getOsg());
        pagamento.setCliente(os.getCliente() != null ? os.getCliente().getNome() : null);
        pagamento.setContrato(os.getContrato() != null
                ? String.valueOf(os.getContrato().getId()) : null);

        // Valores
        pagamento.setValorChamado(valorChamado);
        pagamento.setKm(km);
        pagamento.setValorKm(valorKm);
        pagamento.setPedagio(pedagio);
        pagamento.setEstacionamento(estacionamento);
        pagamento.setOutros(solucao.getOutros() != null ? solucao.getOutros().toPlainString() : null);
        pagamento.setValorOutros(valorOutros);
        pagamento.setValorTotal(valorTotal);

        // Dados do recebimento
        pagamento.setLote(input.getLote());
        pagamento.setNf(input.getNf());
        pagamento.setTipoPagamento(input.getTipoPagamento());
        pagamento.setBanco(input.getBanco());
        pagamento.setDataPrevista(input.getDataPrevista());

        // ✅ dataPagamento: converte LocalDate → OffsetDateTime (meio-dia UTC para evitar virada de dia)
        if (input.getDataPagamento() != null) {
            pagamento.setDataPagamento(
                    input.getDataPagamento().atStartOfDay().atOffset(ZoneOffset.UTC)
            );
        } else {
            pagamento.setDataPagamento(OffsetDateTime.now());
        }

        if (input.getUrlComprovante() != null && !input.getUrlComprovante().isBlank()) {
            pagamento.setUrlComprovante(input.getUrlComprovante());
        }
    }

    private BigDecimal resolverValor(BigDecimal inputValor, BigDecimal defaultValor,
                                     String campo, String osg) {
        if (inputValor != null) return inputValor;
        if (defaultValor != null) return defaultValor;
        log.warn("[PagamentoClienteOS] OS={} | {} não encontrado. Usando ZERO.", osg, campo);
        return BigDecimal.ZERO;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}