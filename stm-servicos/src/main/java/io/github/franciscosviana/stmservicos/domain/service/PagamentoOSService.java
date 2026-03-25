package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.assembler.PagamentoOSOutputAssembler;
import io.github.franciscosviana.stmservicos.api.model.input.PagamentoOSInput;
import io.github.franciscosviana.stmservicos.api.model.output.PagamentoOSOutput;
import io.github.franciscosviana.stmservicos.common.validation.OrdemServicoException;
import io.github.franciscosviana.stmservicos.domain.model.OrdemServico;
import io.github.franciscosviana.stmservicos.domain.model.PagamentoOS;
import io.github.franciscosviana.stmservicos.domain.repository.OrdemServicoRepository;
import io.github.franciscosviana.stmservicos.domain.repository.PagamentoOSRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagamentoOSService {

    private final PagamentoOSRepository pagamentoOSRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final PagamentoOSOutputAssembler pagamentoOSOutputAssembler;

    /**
     * Registra ou atualiza o pagamento de uma OS.
     * <p>
     * Regra de cálculo do valorTotal:
     * valorTotal = valorChamado
     * + (km × valorKm)
     * + pedagio
     * + estacionamento
     * + valorOutros
     * <p>
     * valorChamado e valorKm são resolvidos com a seguinte prioridade:
     * 1. Valor informado no input (sobrescreve)
     * 2. Valor padrão cadastrado no cliente da OS
     */
    @Transactional
    public PagamentoOSOutput registrar(UUID ordemServicoId, PagamentoOSInput input) {

        OrdemServico os = ordemServicoRepository.buscarCompleta(ordemServicoId)
                .orElseThrow(() -> new OrdemServicoException("Ordem de Serviço não encontrada"));

        // Busca pagamento existente ou cria novo
        PagamentoOS pagamento = pagamentoOSRepository
                .findByOrdemServicoId(ordemServicoId)
                .orElse(new PagamentoOS());

        // Resolve valorChamado: input > cliente
        BigDecimal valorChamado = resolverValor(
                input.getValorChamado(),
                os.getCliente() != null ? os.getCliente().getValorChamado() : null,
                "valorChamado",
                os.getOsg()
        );

        // Resolve valorKm: input > cliente
        BigDecimal valorKm = resolverValor(
                input.getValorKm(),
                os.getCliente() != null ? os.getCliente().getValorKm() : null,
                "valorKm",
                os.getOsg()
        );

        BigDecimal km = nvl(input.getKm());
        BigDecimal pedagio = nvl(input.getPedagio());
        BigDecimal estacionamento = nvl(input.getEstacionamento());
        BigDecimal valorOutros = nvl(input.getValorOutros());

        // valorTotal = chamado + (km × valorKm) + pedágio + estacionamento + outros
        BigDecimal totalKm = km.multiply(valorKm);
        BigDecimal valorTotal = valorChamado
                .add(totalKm)
                .add(pedagio)
                .add(estacionamento)
                .add(valorOutros);

        log.info("[PagamentoOS] OS={} | chamado={} | km={}×{}={} | pedágio={} | estac={} | outros={} | TOTAL={}",
                os.getOsg(), valorChamado, km, valorKm, totalKm, pedagio, estacionamento, valorOutros, valorTotal);

        // Preenche dados desnormalizados da OS
        pagamento.setOrdemServico(os);
        pagamento.setOsClt(os.getOsClt());
        pagamento.setOsg(os.getOsg());
        pagamento.setCliente(os.getCliente() != null ? os.getCliente().getNome() : null);
        pagamento.setContrato(os.getContrato() != null ? String.valueOf(os.getContrato().getId()) : null);

        // Preenche valores
        pagamento.setValorChamado(valorChamado);
        pagamento.setKm(km);
        pagamento.setValorKm(valorKm);
        pagamento.setPedagio(pedagio);
        pagamento.setEstacionamento(estacionamento);
        pagamento.setOutros(input.getOutros());
        pagamento.setValorOutros(valorOutros);
        pagamento.setValorTotal(valorTotal);

        // Preenche dados de pagamento
        pagamento.setLote(input.getLote());
        pagamento.setCpfNf(input.getCpfNf());
        pagamento.setTipoPagamento(input.getTipoPagamento());
        pagamento.setBanco(input.getBanco());
        pagamento.setDataPagamento(
                input.getDataPagamento() != null ? input.getDataPagamento() : OffsetDateTime.now()
        );

        // Comprovante: o front faz o upload (S3, Cloudinary, imgbb, etc.)
        // e manda apenas a URL resultante neste campo.
        // Caso o front ainda não tenha enviado o arquivo, mantém a URL anterior.
        if (input.getUrlComprovante() != null && !input.getUrlComprovante().isBlank()) {
            pagamento.setUrlComprovante(input.getUrlComprovante());
        }

        pagamentoOSRepository.save(pagamento);

        return pagamentoOSOutputAssembler.toModel(pagamento);
    }

    public PagamentoOSOutput buscarPorOrdemServico(UUID ordemServicoId) {
        PagamentoOS pagamento = pagamentoOSRepository
                .findByOrdemServicoId(ordemServicoId)
                .orElseThrow(() -> new OrdemServicoException(
                        "Pagamento não encontrado para a OS informada"));

        return pagamentoOSOutputAssembler.toModel(pagamento);
    }

    public Optional<PagamentoOSOutput> buscarPorOrdemServicoOpcional(UUID ordemServicoId) {
        return pagamentoOSRepository
                .findByOrdemServicoId(ordemServicoId)
                .map(pagamentoOSOutputAssembler::toModel);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Retorna o valor do input se não nulo; caso contrário usa o padrão do cliente.
     * Loga um aviso se nenhum dos dois estiver disponível e retorna ZERO.
     */
    private BigDecimal resolverValor(BigDecimal fromInput, BigDecimal fromCliente,
                                     String campo, String osg) {
        if (fromInput != null) return fromInput;
        if (fromCliente != null) {
            log.debug("[PagamentoOS] OS={} | {} não informado no input, usando valor do cliente: {}",
                    osg, campo, fromCliente);
            return fromCliente;
        }
        log.warn("[PagamentoOS] OS={} | {} não informado e cliente não possui valor padrão. Usando ZERO.", osg, campo);
        return BigDecimal.ZERO;
    }

    /**
     * Converte null em ZERO para evitar NPE nos cálculos.
     */
    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}