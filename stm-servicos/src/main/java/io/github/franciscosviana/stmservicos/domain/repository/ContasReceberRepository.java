package io.github.franciscosviana.stmservicos.domain.repository;

import io.github.franciscosviana.stmservicos.api.model.input.ContasReceberFilter;
import io.github.franciscosviana.stmservicos.api.model.output.ContasReceberOutput;
import io.github.franciscosviana.stmservicos.api.model.output.ContasReceberTotaisOutput;
import io.github.franciscosviana.stmservicos.domain.model.Cliente;
import io.github.franciscosviana.stmservicos.domain.model.Contrato;
import io.github.franciscosviana.stmservicos.domain.model.OrdemServico;
import io.github.franciscosviana.stmservicos.domain.model.PagamentoClienteOS;
import io.github.franciscosviana.stmservicos.domain.model.enums.StatusOrdem;
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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ContasReceberRepository {

    private final EntityManager em;

    public Page<ContasReceberOutput> buscarComFiltro(ContasReceberFilter filtro, Pageable pageable) {
        List<ContasReceberOutput> content = buildQuery(filtro, pageable, false);
        long total = countQuery(filtro);
        return new PageImpl<>(content, pageable, total);
    }

    public List<ContasReceberOutput> buscarTodosComFiltro(ContasReceberFilter filtro) {
        return buildQuery(filtro, null, true);
    }

    public List<String> buscarLotesPorCliente(String clienteNome) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<PagamentoClienteOS> pg = cq.from(PagamentoClienteOS.class);
        Join<PagamentoClienteOS, OrdemServico> os = pg.join("ordemServico", JoinType.INNER);

        String pattern = "%" + clienteNome.toLowerCase() + "%";

        cq.select(pg.get("lote")).distinct(true);
        cq.where(
                cb.and(
                        cb.isNotNull(pg.get("lote")),
                        cb.notEqual(pg.get("lote"), ""),
                        cb.isFalse(pg.get("pago")),          // ← era recebido, agora pago
                        cb.like(cb.lower(pg.get("cliente")), pattern)
                )
        );
        return em.createQuery(cq).getResultList();
    }

    public List<String> buscarLotesDistintos() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<PagamentoClienteOS> pg = cq.from(PagamentoClienteOS.class);
        cq.select(pg.get("lote")).distinct(true);
        cq.where(cb.isNotNull(pg.get("lote")));
        return em.createQuery(cq).getResultList();
    }

    // ── Query principal ───────────────────────────────────────────────────────

    private List<ContasReceberOutput> buildQuery(ContasReceberFilter filtro,
                                                 Pageable pageable,
                                                 boolean exportAll) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

        Root<PagamentoClienteOS> pg = cq.from(PagamentoClienteOS.class);
        Join<PagamentoClienteOS, OrdemServico> os = pg.join("ordemServico", JoinType.INNER);
        Join<OrdemServico, Cliente> cli = os.join("cliente", JoinType.LEFT);
        Join<OrdemServico, Contrato> cont = os.join("contrato", JoinType.LEFT);

        cq.multiselect(
                os.get("id"),               //  0
                pg.get("osg"),              //  1
                pg.get("osClt"),            //  2
                pg.get("cliente"),          //  3  snapshot
                cont.get("id"),             //  4
                os.get("status"),           //  5
                os.get("dataHoraAbertura"), //  6
                pg.get("valorChamado"),     //  7
                pg.get("km"),               //  8
                pg.get("valorKm"),          //  9
                pg.get("pedagio"),          // 10
                pg.get("estacionamento"),   // 11
                pg.get("valorOutros"),      // 12
                pg.get("valorTotal"),       // 13
                pg.get("pago"),             // 14
                pg.get("corrigido"),        // 15
                pg.get("tipoPagamento"),    // 16
                pg.get("banco"),            // 17
                pg.get("nf"),               // 18
                pg.get("lote"),             // 19
                pg.get("urlComprovante"),   // 20
                pg.get("dataPrevista"),     // 21
                pg.get("dataPagamento"),    // 22
                pg.get("criadoEm")          // 23
        );

        cq.where(buildPredicates(cb, pg, os, cli, filtro).toArray(new Predicate[0]));
        cq.orderBy(cb.desc(os.get("dataHoraAbertura")));

        TypedQuery<Object[]> query = em.createQuery(cq);
        if (!exportAll && pageable != null) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }

        return query.getResultList().stream().map(this::mapToOutput).toList();
    }

    private long countQuery(ContasReceberFilter filtro) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<PagamentoClienteOS> pg = cq.from(PagamentoClienteOS.class);
        Join<PagamentoClienteOS, OrdemServico> os = pg.join("ordemServico", JoinType.INNER);
        Join<OrdemServico, Cliente> cli = os.join("cliente", JoinType.LEFT);
        cq.select(cb.count(pg));
        cq.where(buildPredicates(cb, pg, os, cli, filtro).toArray(new Predicate[0]));
        return em.createQuery(cq).getSingleResult();
    }

    // ── Predicados ────────────────────────────────────────────────────────────

    private List<Predicate> buildPredicates(CriteriaBuilder cb,
                                            Root<PagamentoClienteOS> pg,
                                            Join<PagamentoClienteOS, OrdemServico> os,
                                            Join<OrdemServico, Cliente> cli,
                                            ContasReceberFilter f) {
        List<Predicate> p = new ArrayList<>();

        p.add(cb.equal(os.get("status"), StatusOrdem.CONCLUIDA));

        if (f == null) return p;

        if (hasText(f.getOsg()))
            p.add(cb.like(cb.lower(pg.get("osg")), like(f.getOsg())));

        if (hasText(f.getOsClt()))
            p.add(cb.like(cb.lower(pg.get("osClt")), like(f.getOsClt())));

        if (hasText(f.getCliente()))
            p.add(cb.like(cb.lower(cli.get("nome")), like(f.getCliente())));

        if (hasText(f.getLote()))
            p.add(cb.equal(pg.get("lote"), f.getLote()));

        if (f.getDataAberturaInicio() != null) {
            OffsetDateTime ini = f.getDataAberturaInicio().atStartOfDay().atOffset(ZoneOffset.UTC);
            p.add(cb.greaterThanOrEqualTo(os.get("dataHoraAbertura"), ini));
        }
        if (f.getDataAberturaFim() != null) {
            OffsetDateTime fim = f.getDataAberturaFim().atTime(23, 59, 59).atOffset(ZoneOffset.UTC);
            p.add(cb.lessThanOrEqualTo(os.get("dataHoraAbertura"), fim));
        }
        if (f.getDataPagamentoInicio() != null) {
            OffsetDateTime ini = f.getDataPagamentoInicio().atStartOfDay().atOffset(ZoneOffset.UTC);
            p.add(cb.greaterThanOrEqualTo(pg.get("dataPagamento"), ini));
        }
        if (f.getDataPagamentoFim() != null) {
            OffsetDateTime fim = f.getDataPagamentoFim().atTime(23, 59, 59).atOffset(ZoneOffset.UTC);
            p.add(cb.lessThanOrEqualTo(pg.get("dataPagamento"), fim));
        }

        if (hasText(f.getClienteSnapshot()))
            p.add(cb.like(cb.lower(pg.get("cliente")), like(f.getClienteSnapshot())));

        if (f.getPago() != null)
            p.add(cb.equal(pg.get("pago"), f.getPago()));

        return p;
    }

    // ── Mapeamento ────────────────────────────────────────────────────────────

    private ContasReceberOutput mapToOutput(Object[] r) {
        ContasReceberOutput o = new ContasReceberOutput();
        o.setOrdemServicoId(r[0] != null ? (UUID) r[0] : null);
        o.setOsg((String) r[1]);
        o.setOsClt((String) r[2]);
        o.setCliente((String) r[3]);
        o.setContrato(r[4] != null ? String.valueOf(r[4]) : null);
        o.setStatusOrdem(r[5] != null ? (StatusOrdem) r[5] : null);
        o.setDataHoraAbertura((OffsetDateTime) r[6]);
        o.setValorChamado(toBD(r[7]));
        o.setKm(toBD(r[8]));
        o.setValorKm(toBD(r[9]));
        o.setPedagio(toBD(r[10]));
        o.setEstacionamento(toBD(r[11]));
        o.setValorOutros(toBD(r[12]));
        o.setValorTotal(toBD(r[13]));
        o.setPago(r[14] != null && (boolean) r[14]);
        o.setCorrigido(r[15] != null && (boolean) r[15]);
        o.setTipoPagamento(r[16] != null ? (TipoPagamento) r[16] : null);
        o.setBanco((String) r[17]);
        o.setNf((String) r[18]);
        o.setLote((String) r[19]);
        o.setUrlComprovante((String) r[20]);
        o.setDataPrevista(r[21] != null ? (LocalDate) r[21] : null);
        o.setDataPagamento(r[22] != null ? (OffsetDateTime) r[22] : null);
        o.setCriadoEm(r[23] != null ? (OffsetDateTime) r[23] : null);
        return o;
    }

    // ── Totais ────────────────────────────────────────────────────────────────

    public ContasReceberTotaisOutput buscarTotais(ContasReceberFilter filtro) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

        Root<PagamentoClienteOS> pg = cq.from(PagamentoClienteOS.class);
        Join<PagamentoClienteOS, OrdemServico> os = pg.join("ordemServico", JoinType.INNER);
        Join<OrdemServico, Cliente> cli = os.join("cliente", JoinType.LEFT);

        Expression<BigDecimal> zero = cb.literal(BigDecimal.ZERO);

        // RECEBIDO
        Expression<BigDecimal> recChamado = cb.<BigDecimal>selectCase()
                .when(cb.isTrue(pg.get("pago")), pg.<BigDecimal>get("valorChamado")).otherwise(zero);
        Expression<BigDecimal> recKmValor = cb.<BigDecimal>selectCase()
                .when(cb.isTrue(pg.get("pago")),
                        cb.prod(pg.<BigDecimal>get("km"), pg.<BigDecimal>get("valorKm"))).otherwise(zero);
        Expression<BigDecimal> recPedagio = cb.<BigDecimal>selectCase()
                .when(cb.isTrue(pg.get("pago")), pg.<BigDecimal>get("pedagio")).otherwise(zero);
        Expression<BigDecimal> recEstac = cb.<BigDecimal>selectCase()
                .when(cb.isTrue(pg.get("pago")), pg.<BigDecimal>get("estacionamento")).otherwise(zero);
        Expression<BigDecimal> recOutros = cb.<BigDecimal>selectCase()
                .when(cb.isTrue(pg.get("pago")), pg.<BigDecimal>get("valorOutros")).otherwise(zero);
        Expression<BigDecimal> recTotal = cb.<BigDecimal>selectCase()
                .when(cb.isTrue(pg.get("pago")), pg.<BigDecimal>get("valorTotal")).otherwise(zero);

        // NÃO RECEBIDO
        Expression<BigDecimal> naoChamado = cb.<BigDecimal>selectCase()
                .when(cb.isFalse(pg.get("pago")), pg.<BigDecimal>get("valorChamado")).otherwise(zero);
        Expression<BigDecimal> naoKmValor = cb.<BigDecimal>selectCase()
                .when(cb.isFalse(pg.get("pago")),
                        cb.prod(pg.<BigDecimal>get("km"), pg.<BigDecimal>get("valorKm"))).otherwise(zero);
        Expression<BigDecimal> naoPedagio = cb.<BigDecimal>selectCase()
                .when(cb.isFalse(pg.get("pago")), pg.<BigDecimal>get("pedagio")).otherwise(zero);
        Expression<BigDecimal> naoEstac = cb.<BigDecimal>selectCase()
                .when(cb.isFalse(pg.get("pago")), pg.<BigDecimal>get("estacionamento")).otherwise(zero);
        Expression<BigDecimal> naoOutros = cb.<BigDecimal>selectCase()
                .when(cb.isFalse(pg.get("pago")), pg.<BigDecimal>get("valorOutros")).otherwise(zero);
        Expression<BigDecimal> naoTotal = cb.<BigDecimal>selectCase()
                .when(cb.isFalse(pg.get("pago")), pg.<BigDecimal>get("valorTotal")).otherwise(zero);

        cq.multiselect(
                cb.count(pg),                //  0
                cb.sum(recTotal),            //  1
                cb.sum(naoTotal),            //  2
                cb.sum(pg.get("valorTotal")),//  3
                cb.sum(cb.<Long>selectCase()
                        .when(cb.isTrue(pg.get("pago")), 1L).otherwise(0L)),  //  4
                cb.sum(cb.<Long>selectCase()
                        .when(cb.isFalse(pg.get("pago")), 1L).otherwise(0L)), //  5
                cb.sum(recChamado),  //  6
                cb.sum(recKmValor),  //  7
                cb.sum(recPedagio),  //  8
                cb.sum(recEstac),    //  9
                cb.sum(recOutros),   // 10
                cb.sum(naoChamado),  // 11
                cb.sum(naoKmValor),  // 12
                cb.sum(naoPedagio),  // 13
                cb.sum(naoEstac),    // 14
                cb.sum(naoOutros)    // 15
        );

        cq.where(buildPredicates(cb, pg, os, cli, filtro).toArray(new Predicate[0]));

        Object[] r = em.createQuery(cq).getSingleResult();

        return ContasReceberTotaisOutput.builder()
                .qtdTotal(toLong(r[0]))
                .totalRecebido(toBD(r[1]))
                .totalNaoRecebido(toBD(r[2]))
                .totalGeral(toBD(r[3]))
                .qtdRecebido(toLong(r[4]))
                .qtdNaoRecebido(toLong(r[5]))
                .recebidoValorChamado(toBD(r[6]))
                .recebidoValorKm(toBD(r[7]))
                .recebidoPedagio(toBD(r[8]))
                .recebidoEstacionamento(toBD(r[9]))
                .recebidoOutros(toBD(r[10]))
                .naoRecebidoValorChamado(toBD(r[11]))
                .naoRecebidoValorKm(toBD(r[12]))
                .naoRecebidoPedagio(toBD(r[13]))
                .naoRecebidoEstacionamento(toBD(r[14]))
                .naoRecebidoOutros(toBD(r[15]))
                .build();
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

    private long toLong(Object o) {
        if (o instanceof Number n) return n.longValue();
        return 0L;
    }
}