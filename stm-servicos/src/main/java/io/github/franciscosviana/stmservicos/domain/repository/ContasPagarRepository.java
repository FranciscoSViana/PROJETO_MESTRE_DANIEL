package io.github.franciscosviana.stmservicos.domain.repository;

import io.github.franciscosviana.stmservicos.api.model.input.ContasPagarFilter;
import io.github.franciscosviana.stmservicos.api.model.output.ContasPagarOutput;
import io.github.franciscosviana.stmservicos.domain.model.*;
import io.github.franciscosviana.stmservicos.domain.model.enums.StatusOrdem;
import io.github.franciscosviana.stmservicos.domain.model.enums.TipoFluxoPagamento;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ContasPagarRepository {

    private final EntityManager em;

    public Page<ContasPagarOutput> buscarComFiltro(ContasPagarFilter filtro, Pageable pageable) {
        List<ContasPagarOutput> content = buildQuery(filtro, pageable, false);
        long total = countQuery(filtro);
        return new PageImpl<>(content, pageable, total);
    }

    public List<ContasPagarOutput> buscarTodosComFiltro(ContasPagarFilter filtro) {
        return buildQuery(filtro, null, true);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Query principal (Criteria API) — raiz em PagamentoOS
    // ─────────────────────────────────────────────────────────────────────────
    private List<ContasPagarOutput> buildQuery(ContasPagarFilter filtro,
                                               Pageable pageable,
                                               boolean exportAll) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

        Root<PagamentoOS> pg = cq.from(PagamentoOS.class);
        Join<PagamentoOS, OrdemServico> osJoin = pg.join("ordemServico", JoinType.INNER);
        Join<OrdemServico, Cliente> clienteJoin = osJoin.join("cliente", JoinType.LEFT);
        Join<OrdemServico, Credenciado> credenciadoJoin = osJoin.join("credenciado", JoinType.LEFT);
        Join<OrdemServico, Contrato> contratoJoin = osJoin.join("contrato", JoinType.LEFT);

        cq.multiselect(
                osJoin.get("id"),                              // 0
                osJoin.get("osg"),                             // 1
                osJoin.get("osClt"),                           // 2
                clienteJoin.get("nome"),                       // 3
                contratoJoin.get("id"),                        // 4
                credenciadoJoin.get("id"),                     // 5
                credenciadoJoin.get("codigo"),                 // 6
                credenciadoJoin.get("rag"),                    // 7
                credenciadoJoin.get("tipoFluxoPagamento"),     // 8
                osJoin.get("status"),                          // 9
                osJoin.get("dataHoraAbertura"),                // 10
                pg.get("valorChamado"),                        // 11
                pg.get("km"),                                  // 12
                pg.get("valorKm"),                             // 13
                pg.get("pedagio"),                             // 14
                pg.get("estacionamento"),                      // 15
                pg.get("valorOutros"),                         // 16
                pg.get("valorTotal"),                          // 17
                pg.get("pago"),                                // 18
                pg.get("tipoPagamento"),                       // 19
                pg.get("banco"),                               // 20
                pg.get("chavePix"),                            // 21
                pg.get("cpfNf"),                               // 22
                pg.get("lote"),                                // 23
                pg.get("urlComprovante"),                      // 24
                pg.get("dataPagamento"),                       // 25
                pg.get("criadoEm")                             // 26
        );

        List<Predicate> predicates = buildPredicates(cb, osJoin, pg, credenciadoJoin, clienteJoin, filtro);
        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.desc(osJoin.get("dataHoraAbertura")));

        TypedQuery<Object[]> query = em.createQuery(cq);

        if (!exportAll && pageable != null) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }

        return query.getResultList().stream()
                .map(this::mapToOutput)
                .toList();
    }

    private long countQuery(ContasPagarFilter filtro) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<PagamentoOS> pg = cq.from(PagamentoOS.class);
        Join<PagamentoOS, OrdemServico> osJoin = pg.join("ordemServico", JoinType.INNER);
        Join<OrdemServico, Credenciado> credenciadoJoin = osJoin.join("credenciado", JoinType.LEFT);
        Join<OrdemServico, Cliente> clienteJoin = osJoin.join("cliente", JoinType.LEFT);

        cq.select(cb.count(pg));
        List<Predicate> predicates = buildPredicates(cb, osJoin, pg, credenciadoJoin, clienteJoin, filtro);
        cq.where(predicates.toArray(new Predicate[0]));

        return em.createQuery(cq).getSingleResult();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Predicates
    // ─────────────────────────────────────────────────────────────────────────
    private List<Predicate> buildPredicates(CriteriaBuilder cb,
                                            Join<PagamentoOS, OrdemServico> os,
                                            Root<PagamentoOS> pg,
                                            Join<OrdemServico, Credenciado> credenciado,
                                            Join<OrdemServico, Cliente> cliente,
                                            ContasPagarFilter f) {
        List<Predicate> p = new ArrayList<>();
        if (f == null) return p;

        if (hasText(f.getOsg()))
            p.add(cb.like(cb.lower(os.get("osg")), like(f.getOsg())));

        if (hasText(f.getOsClt()))
            p.add(cb.like(cb.lower(os.get("osClt")), like(f.getOsClt())));

        if (hasText(f.getCliente()))
            p.add(cb.like(cb.lower(cliente.get("nome")), like(f.getCliente())));

        if (hasText(f.getCredenciado()))
            p.add(cb.like(cb.lower(credenciado.get("rag")), like(f.getCredenciado())));

        if (f.getPago() != null)
            p.add(cb.equal(pg.get("pago"), f.getPago()));

        // ✅ CORRIGIDO: filtra por pg.lote (campo da tabela pagamento_os)
        if (hasText(f.getLote()))
            p.add(cb.equal(pg.get("lote"), f.getLote()));

        if (f.getDataInicio() != null) {
            OffsetDateTime inicio = f.getDataInicio().atStartOfDay().atOffset(ZoneOffset.UTC);
            p.add(cb.greaterThanOrEqualTo(pg.get("dataPagamento"), inicio));
        }

        if (f.getDataFim() != null) {
            OffsetDateTime fim = f.getDataFim().atTime(23, 59, 59).atOffset(ZoneOffset.UTC);
            p.add(cb.lessThanOrEqualTo(pg.get("dataPagamento"), fim));
        }

        return p;
    }

    // ── Mapper ───────────────────────────────────────────────────────────────
    private ContasPagarOutput mapToOutput(Object[] r) {
        ContasPagarOutput o = new ContasPagarOutput();
        o.setOrdemServicoId(r[0] != null ? (java.util.UUID) r[0] : null);
        o.setOsg((String) r[1]);
        o.setOsClt((String) r[2]);
        o.setCliente((String) r[3]);
        o.setContrato(r[4] != null ? String.valueOf(r[4]) : null);
        o.setCredenciadoId(r[5] != null ? (java.util.UUID) r[5] : null);
        o.setCredenciadoCodigo(r[6] != null ? ((Number) r[6]).longValue() : null);
        o.setCredenciadoRag((String) r[7]);
        o.setTipoFluxoPagamento(r[8] != null ? (TipoFluxoPagamento) r[8] : null);
        o.setStatusOrdem(r[9] != null ? (StatusOrdem) r[9] : null);
        o.setDataHoraAbertura((OffsetDateTime) r[10]);
        o.setValorChamado(toBD(r[11]));
        o.setKm(toBD(r[12]));
        o.setValorKm(toBD(r[13]));
        o.setPedagio(toBD(r[14]));
        o.setEstacionamento(toBD(r[15]));
        o.setValorOutros(toBD(r[16]));
        o.setValorTotal(toBD(r[17]));
        o.setPago(r[18] != null && (boolean) r[18]);
        o.setTipoPagamento(r[19] != null
                ? (io.github.franciscosviana.stmservicos.domain.model.enums.TipoPagamento) r[19] : null);
        o.setBanco((String) r[20]);
        o.setChavePix((String) r[21]);
        o.setCpfNf((String) r[22]);
        o.setLote((String) r[23]);
        o.setUrlComprovante((String) r[24]);
        o.setDataPagamento((OffsetDateTime) r[25]);
        o.setPagamentoCriadoEm((OffsetDateTime) r[26]);
        return o;
    }

    // ── Utils ────────────────────────────────────────────────────────────────
    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    private String like(String s) {
        return "%" + s.toLowerCase() + "%";
    }

    private java.math.BigDecimal toBD(Object o) {
        return o instanceof java.math.BigDecimal bd ? bd : java.math.BigDecimal.ZERO;
    }
}