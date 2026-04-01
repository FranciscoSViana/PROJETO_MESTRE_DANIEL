package io.github.franciscosviana.stmservicos.domain.repository;

import io.github.franciscosviana.stmservicos.api.model.input.ContasPagarFilter;
import io.github.franciscosviana.stmservicos.api.model.output.ContasPagarOutput;
import io.github.franciscosviana.stmservicos.api.model.output.ContasPagarTotaisOutput;
import io.github.franciscosviana.stmservicos.domain.model.*;
import io.github.franciscosviana.stmservicos.domain.model.enums.StatusOrdem;
import io.github.franciscosviana.stmservicos.domain.model.enums.TipoFluxoPagamento;
import io.github.franciscosviana.stmservicos.domain.model.enums.TipoPagamento;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ContasPagarRepository {

    private final EntityManager em;

    // ── API pública ───────────────────────────────────────────────────────────

    public Page<ContasPagarOutput> buscarComFiltro(ContasPagarFilter filtro, Pageable pageable) {
        List<ContasPagarOutput> content = buildQuery(filtro, pageable, false);
        long total = countQuery(filtro);
        return new PageImpl<>(content, pageable, total);
    }

    public List<ContasPagarOutput> buscarTodosComFiltro(ContasPagarFilter filtro) {
        return buildQuery(filtro, null, true);
    }

    public List<String> buscarLotesDistintos() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<PagamentoOS> pg = cq.from(PagamentoOS.class);
        cq.select(pg.get("lote")).distinct(true);
        cq.where(cb.isNotNull(pg.get("lote")));
        return em.createQuery(cq).getResultList();
    }

    // ── Query principal (raiz = PagamentoOS) ─────────────────────────────────

    private List<ContasPagarOutput> buildQuery(ContasPagarFilter filtro,
                                               Pageable pageable,
                                               boolean exportAll) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

        // Raiz: PagamentoOS — sempre existe para OS concluídas após a refatoração
        Root<PagamentoOS> pg = cq.from(PagamentoOS.class);

        // Joins necessários para filtros e projeção
        Join<PagamentoOS, OrdemServico> os = pg.join("ordemServico", JoinType.INNER);
        Join<OrdemServico, Cliente> cli = os.join("cliente", JoinType.LEFT);
        Join<OrdemServico, Credenciado> cred = os.join("credenciado", JoinType.LEFT);
        Join<OrdemServico, Contrato> cont = os.join("contrato", JoinType.LEFT);

        cq.multiselect(
                os.get("id"),                       //  0 – UUID da OS
                pg.get("osg"),                      //  1
                pg.get("osClt"),                    //  2
                pg.get("cliente"),                  //  3  snapshot
                cont.get("id"),                     //  4
                cred.get("id"),                     //  5
                cred.get("codigo"),                 //  6
                cred.get("rag"),                    //  7
                cred.get("tipoFluxoPagamento"),     //  8
                os.get("status"),                   //  9
                os.get("dataHoraAbertura"),         // 10
                pg.get("valorChamado"),             // 11
                pg.get("km"),                       // 12
                pg.get("valorKm"),                  // 13
                pg.get("pedagio"),                  // 14
                pg.get("estacionamento"),           // 15
                pg.get("valorOutros"),              // 16
                pg.get("valorTotal"),               // 17
                pg.get("pago"),                     // 18
                pg.get("tipoPagamento"),            // 19
                pg.get("banco"),                    // 20
                pg.get("chavePix"),                 // 21
                pg.get("cpfNf"),                    // 22
                pg.get("lote"),                     // 23
                pg.get("urlComprovante"),           // 24
                pg.get("dataPagamento"),            // 25
                pg.get("criadoEm")                  // 26
        );

        cq.where(buildPredicates(cb, pg, os, cred, cli, filtro).toArray(new Predicate[0]));
        cq.orderBy(cb.desc(os.get("dataHoraAbertura")));

        TypedQuery<Object[]> query = em.createQuery(cq);
        if (!exportAll && pageable != null) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }

        return query.getResultList().stream().map(this::mapToOutput).toList();
    }

    private long countQuery(ContasPagarFilter filtro) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);

        Root<PagamentoOS> pg = cq.from(PagamentoOS.class);
        Join<PagamentoOS, OrdemServico> os = pg.join("ordemServico", JoinType.INNER);
        Join<OrdemServico, Cliente> cli = os.join("cliente", JoinType.LEFT);
        Join<OrdemServico, Credenciado> cred = os.join("credenciado", JoinType.LEFT);

        cq.select(cb.count(pg));
        cq.where(buildPredicates(cb, pg, os, cred, cli, filtro).toArray(new Predicate[0]));

        return em.createQuery(cq).getSingleResult();
    }

    // ── Predicados de filtro ──────────────────────────────────────────────────

    private List<Predicate> buildPredicates(CriteriaBuilder cb,
                                            Root<PagamentoOS> pg,
                                            Join<PagamentoOS, OrdemServico> os,
                                            Join<OrdemServico, Credenciado> cred,
                                            Join<OrdemServico, Cliente> cli,
                                            ContasPagarFilter f) {
        List<Predicate> p = new ArrayList<>();

        // Apenas OS concluídas — garantia extra além do rascunho
        p.add(cb.equal(os.get("status"), StatusOrdem.CONCLUIDA));

        if (f == null) return p;

        if (hasText(f.getOsg()))
            p.add(cb.like(cb.lower(pg.get("osg")), like(f.getOsg())));

        if (hasText(f.getOsClt()))
            p.add(cb.like(cb.lower(pg.get("osClt")), like(f.getOsClt())));

        if (hasText(f.getCliente()))
            p.add(cb.like(cb.lower(cli.get("nome")), like(f.getCliente())));

        if (hasText(f.getCredenciado()))
            p.add(cb.like(cb.lower(cred.get("rag")), like(f.getCredenciado())));

        if (f.getPago() != null) {
            p.add(cb.equal(pg.get("pago"), f.getPago()));
        }

        if (hasText(f.getLote()))
            p.add(cb.equal(pg.get("lote"), f.getLote()));

        if (f.getDataAberturaInicio() != null) {
            OffsetDateTime inicio = f.getDataAberturaInicio().atStartOfDay().atOffset(ZoneOffset.UTC);
            p.add(cb.greaterThanOrEqualTo(os.get("dataHoraAbertura"), inicio));
        }
        if (f.getDataAberturaFim() != null) {
            OffsetDateTime fim = f.getDataAberturaFim().atTime(23, 59, 59).atOffset(ZoneOffset.UTC);
            p.add(cb.lessThanOrEqualTo(os.get("dataHoraAbertura"), fim));
        }

        if (f.getDataPagamentoInicio() != null) {
            OffsetDateTime inicio = f.getDataPagamentoInicio().atStartOfDay().atOffset(ZoneOffset.UTC);
            p.add(cb.greaterThanOrEqualTo(pg.get("dataPagamento"), inicio));
        }
        if (f.getDataPagamentoFim() != null) {
            OffsetDateTime fim = f.getDataPagamentoFim().atTime(23, 59, 59).atOffset(ZoneOffset.UTC);
            p.add(cb.lessThanOrEqualTo(pg.get("dataPagamento"), fim));
        }

        return p;
    }

    // ── Mapeamento de resultado ───────────────────────────────────────────────

    private ContasPagarOutput mapToOutput(Object[] r) {
        ContasPagarOutput o = new ContasPagarOutput();

        o.setOrdemServicoId(r[0] != null ? (UUID) r[0] : null);
        o.setOsg((String) r[1]);
        o.setOsClt((String) r[2]);
        o.setCliente((String) r[3]);
        o.setContrato(r[4] != null ? String.valueOf(r[4]) : null);
        o.setCredenciadoId(r[5] != null ? (UUID) r[5] : null);
        o.setCredenciadoCodigo(r[6] != null ? ((Number) r[6]).longValue() : null);
        o.setCredenciadoRag((String) r[7]);
        o.setTipoFluxoPagamento(r[8] != null ? (TipoFluxoPagamento) r[8] : null);
        o.setStatusOrdem(r[9] != null ? (StatusOrdem) r[9] : null);
        o.setDataHoraAbertura((OffsetDateTime) r[10]);

        // Valores — sempre presentes pois o rascunho já os preenche
        o.setValorChamado(toBD(r[11]));
        o.setKm(toBD(r[12]));
        o.setValorKm(toBD(r[13]));
        o.setPedagio(toBD(r[14]));
        o.setEstacionamento(toBD(r[15]));
        o.setValorOutros(toBD(r[16]));
        o.setValorTotal(toBD(r[17]));

        o.setPago(r[18] != null && (boolean) r[18]);
        o.setTipoPagamento(r[19] != null ? (TipoPagamento) r[19] : null);
        o.setBanco((String) r[20]);
        o.setChavePix((String) r[21]);
        o.setCpfNf((String) r[22]);
        o.setLote((String) r[23]);
        o.setUrlComprovante((String) r[24]);
        o.setDataPagamento((OffsetDateTime) r[25]);
        o.setPagamentoCriadoEm((OffsetDateTime) r[26]);

        return o;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    private String like(String s) {
        return "%" + s.toLowerCase() + "%";
    }

    private BigDecimal toBD(Object o) {
        if (o instanceof BigDecimal bd) return bd;
        if (o instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return BigDecimal.ZERO;
    }

    public ContasPagarTotaisOutput buscarTotais(ContasPagarFilter filtro) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

        Root<PagamentoOS> pg = cq.from(PagamentoOS.class);
        Join<PagamentoOS, OrdemServico> os = pg.join("ordemServico", JoinType.INNER);
        Join<OrdemServico, Cliente> cli = os.join("cliente", JoinType.LEFT);
        Join<OrdemServico, Credenciado> cred = os.join("credenciado", JoinType.LEFT);

        // Expressões condicionais para separar pago / não pago
        Expression<BigDecimal> zero = cb.literal(BigDecimal.ZERO);

        // PAGO
        Expression<BigDecimal> pagoChamado = cb.<BigDecimal>selectCase()
                .when(cb.isTrue(pg.get("pago")), pg.<BigDecimal>get("valorChamado")).otherwise(zero);
        Expression<BigDecimal> pagoKm = cb.<BigDecimal>selectCase()
                .when(cb.isTrue(pg.get("pago")), pg.<BigDecimal>get("km")).otherwise(zero);
        Expression<BigDecimal> pagoKmValor = cb.<BigDecimal>selectCase()
                .when(cb.isTrue(pg.get("pago")),
                        cb.prod(pg.<BigDecimal>get("km"), pg.<BigDecimal>get("valorKm")))
                .otherwise(zero);
        Expression<BigDecimal> pagoPedagio = cb.<BigDecimal>selectCase()
                .when(cb.isTrue(pg.get("pago")), pg.<BigDecimal>get("pedagio")).otherwise(zero);
        Expression<BigDecimal> pagoEstac = cb.<BigDecimal>selectCase()
                .when(cb.isTrue(pg.get("pago")), pg.<BigDecimal>get("estacionamento")).otherwise(zero);
        Expression<BigDecimal> pagoOutros = cb.<BigDecimal>selectCase()
                .when(cb.isTrue(pg.get("pago")), pg.<BigDecimal>get("valorOutros")).otherwise(zero);
        Expression<BigDecimal> pagoTotal = cb.<BigDecimal>selectCase()
                .when(cb.isTrue(pg.get("pago")), pg.<BigDecimal>get("valorTotal")).otherwise(zero);

        // NÃO PAGO
        Expression<BigDecimal> naoChamado = cb.<BigDecimal>selectCase()
                .when(cb.isFalse(pg.get("pago")), pg.<BigDecimal>get("valorChamado")).otherwise(zero);
        Expression<BigDecimal> naoKm = cb.<BigDecimal>selectCase()
                .when(cb.isFalse(pg.get("pago")), pg.<BigDecimal>get("km")).otherwise(zero);
        Expression<BigDecimal> naoKmValor = cb.<BigDecimal>selectCase()
                .when(cb.isFalse(pg.get("pago")),
                        cb.prod(pg.<BigDecimal>get("km"), pg.<BigDecimal>get("valorKm")))
                .otherwise(zero);
        Expression<BigDecimal> naoPedagio = cb.<BigDecimal>selectCase()
                .when(cb.isFalse(pg.get("pago")), pg.<BigDecimal>get("pedagio")).otherwise(zero);
        Expression<BigDecimal> naoEstac = cb.<BigDecimal>selectCase()
                .when(cb.isFalse(pg.get("pago")), pg.<BigDecimal>get("estacionamento")).otherwise(zero);
        Expression<BigDecimal> naoOutros = cb.<BigDecimal>selectCase()
                .when(cb.isFalse(pg.get("pago")), pg.<BigDecimal>get("valorOutros")).otherwise(zero);
        Expression<BigDecimal> naoTotal = cb.<BigDecimal>selectCase()
                .when(cb.isFalse(pg.get("pago")), pg.<BigDecimal>get("valorTotal")).otherwise(zero);

        cq.multiselect(
                cb.count(pg),               //  0 – qtd total
                cb.sum(pagoTotal),          //  1 – total pago
                cb.sum(naoTotal),           //  2 – total não pago
                cb.sum(pg.get("valorTotal")),//  3 – total geral

                // contagens
                cb.sum(cb.<Long>selectCase()
                        .when(cb.isTrue(pg.get("pago")), 1L).otherwise(0L)),  //  4 – qtd pago
                cb.sum(cb.<Long>selectCase()
                        .when(cb.isFalse(pg.get("pago")), 1L).otherwise(0L)), //  5 – qtd não pago

                // destrinchado pago
                cb.sum(pagoChamado),   //  6
                cb.sum(pagoKm),        //  7
                cb.sum(pagoKmValor),   //  8
                cb.sum(pagoPedagio),   //  9
                cb.sum(pagoEstac),     // 10
                cb.sum(pagoOutros),    // 11

                // destrinchado não pago
                cb.sum(naoChamado),    // 12
                cb.sum(naoKm),         // 13
                cb.sum(naoKmValor),    // 14
                cb.sum(naoPedagio),    // 15
                cb.sum(naoEstac),      // 16
                cb.sum(naoOutros)      // 17
        );

        cq.where(buildPredicates(cb, pg, os, cred, cli, filtro).toArray(new Predicate[0]));

        Object[] r = em.createQuery(cq).getSingleResult();

        return ContasPagarTotaisOutput.builder()
                .qtdTotal(toLong(r[0]))
                .totalPago(toBD(r[1]))
                .totalNaoPago(toBD(r[2]))
                .totalGeral(toBD(r[3]))
                .qtdPago(toLong(r[4]))
                .qtdNaoPago(toLong(r[5]))
                // pago
                .pagoValorChamado(toBD(r[6]))
                .pagoKmTotal(toBD(r[7]))
                .pagoValorKm(toBD(r[8]))
                .pagoPedagio(toBD(r[9]))
                .pagoEstacionamento(toBD(r[10]))
                .pagoOutros(toBD(r[11]))
                // não pago
                .naoPagoValorChamado(toBD(r[12]))
                .naoPagoKmTotal(toBD(r[13]))
                .naoPagoValorKm(toBD(r[14]))
                .naoPagoPedagio(toBD(r[15]))
                .naoPagoEstacionamento(toBD(r[16]))
                .naoPagoOutros(toBD(r[17]))
                .build();
    }

    // helper adicional (já tem toBD, adicionar toLong)
    private long toLong(Object o) {
        if (o instanceof Number n) return n.longValue();
        return 0L;
    }
}