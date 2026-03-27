package io.github.franciscosviana.stmservicos.domain.repository;

import io.github.franciscosviana.stmservicos.api.model.input.ContasPagarFilter;
import io.github.franciscosviana.stmservicos.api.model.output.ContasPagarOutput;
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

    public Page<ContasPagarOutput> buscarComFiltro(ContasPagarFilter filtro, Pageable pageable) {
        List<ContasPagarOutput> content = buildQuery(filtro, pageable, false);
        long total = countQuery(filtro);
        return new PageImpl<>(content, pageable, total);
    }

    public List<ContasPagarOutput> buscarTodosComFiltro(ContasPagarFilter filtro) {
        return buildQuery(filtro, null, true);
    }

    private List<ContasPagarOutput> buildQuery(ContasPagarFilter filtro,
                                               Pageable pageable,
                                               boolean exportAll) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);

        Root<OrdemServico> os = cq.from(OrdemServico.class);
        Join<OrdemServico, Cliente> clienteJoin = os.join("cliente", JoinType.LEFT);
        Join<OrdemServico, Credenciado> credJoin = os.join("credenciado", JoinType.LEFT);
        Join<OrdemServico, Contrato> contratoJoin = os.join("contrato", JoinType.LEFT);
        Join<OrdemServico, PagamentoOS> pg = os.join("pagamento", JoinType.LEFT);
        Join<OrdemServico, SolucaoOS> solucaoJoin = os.join("solucao", JoinType.LEFT);

        cq.multiselect(
                os.get("id"),                               // 0
                os.get("osg"),                              // 1
                os.get("osClt"),                            // 2
                clienteJoin.get("nome"),                    // 3
                contratoJoin.get("id"),                     // 4
                credJoin.get("id"),                         // 5
                credJoin.get("codigo"),                     // 6
                credJoin.get("rag"),                        // 7
                credJoin.get("tipoFluxoPagamento"),         // 8
                os.get("status"),                           // 9
                os.get("dataHoraAbertura"),                 // 10
                pg.get("valorChamado"),                     // 11
                pg.get("km"),                               // 12
                pg.get("valorKm"),                          // 13
                pg.get("pedagio"),                          // 14
                pg.get("estacionamento"),                   // 15
                pg.get("valorOutros"),                      // 16
                pg.get("valorTotal"),                       // 17
                pg.get("pago"),                             // 18
                pg.get("tipoPagamento"),                    // 19
                pg.get("banco"),                            // 20
                pg.get("chavePix"),                         // 21
                pg.get("cpfNf"),                            // 22
                pg.get("lote"),                             // 23
                pg.get("urlComprovante"),                   // 24
                pg.get("dataPagamento"),                    // 25
                pg.get("criadoEm"),                         // 26
                credJoin.get("valorChamado"),               // 27
                credJoin.get("valorKm"),                    // 28
                solucaoJoin.get("km"),                      // 29
                solucaoJoin.get("pedagio"),                 // 30
                solucaoJoin.get("estacionamento"),          // 31
                solucaoJoin.get("outros")                   // 32
        );

        List<Predicate> predicates = buildPredicates(cb, os, pg, credJoin, clienteJoin, filtro);
        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.desc(os.get("dataHoraAbertura")));

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

        Root<OrdemServico> os = cq.from(OrdemServico.class);
        Join<OrdemServico, Credenciado> credJoin = os.join("credenciado", JoinType.LEFT);
        Join<OrdemServico, Cliente> clienteJoin = os.join("cliente", JoinType.LEFT);
        Join<OrdemServico, PagamentoOS> pg = os.join("pagamento", JoinType.LEFT);

        cq.select(cb.count(os));
        cq.where(buildPredicates(cb, os, pg, credJoin, clienteJoin, filtro)
                .toArray(new Predicate[0]));

        return em.createQuery(cq).getSingleResult();
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb,
                                            Root<OrdemServico> os,
                                            Join<OrdemServico, PagamentoOS> pg,
                                            Join<OrdemServico, Credenciado> credenciado,
                                            Join<OrdemServico, Cliente> cliente,
                                            ContasPagarFilter f) {
        List<Predicate> p = new ArrayList<>();

        p.add(cb.equal(os.get("status"), StatusOrdem.CONCLUIDA));

        if (f == null) return p;

        if (hasText(f.getOsg()))
            p.add(cb.like(cb.lower(os.get("osg")), like(f.getOsg())));

        if (hasText(f.getOsClt()))
            p.add(cb.like(cb.lower(os.get("osClt")), like(f.getOsClt())));

        if (hasText(f.getCliente()))
            p.add(cb.like(cb.lower(cliente.get("nome")), like(f.getCliente())));

        if (hasText(f.getCredenciado()))
            p.add(cb.like(cb.lower(credenciado.get("rag")), like(f.getCredenciado())));

        if (f.getPago() != null) {
            if (Boolean.FALSE.equals(f.getPago())) {
                p.add(cb.or(
                        pg.get("pago").isNull(),
                        cb.equal(pg.get("pago"), false)
                ));
            } else {
                p.add(cb.equal(pg.get("pago"), true));
            }
        }

        if (hasText(f.getLote()))
            p.add(cb.equal(pg.get("lote"), f.getLote()));

        // Data Abertura
        if (f.getDataAberturaInicio() != null) {
            OffsetDateTime inicio = f.getDataAberturaInicio().atStartOfDay().atOffset(ZoneOffset.UTC);
            p.add(cb.greaterThanOrEqualTo(os.get("dataHoraAbertura"), inicio));
        }
        if (f.getDataAberturaFim() != null) {
            OffsetDateTime fim = f.getDataAberturaFim().atTime(23, 59, 59).atOffset(ZoneOffset.UTC);
            p.add(cb.lessThanOrEqualTo(os.get("dataHoraAbertura"), fim));
        }

        // Data Pagamento
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

        boolean temPagamento = r[18] != null;

        if (temPagamento) {
            o.setValorChamado(toBD(r[11]));
            o.setKm(toBD(r[12]));
            o.setValorKm(toBD(r[13]));
            o.setPedagio(toBD(r[14]));
            o.setEstacionamento(toBD(r[15]));
            o.setValorOutros(toBD(r[16]));
            o.setValorTotal(toBD(r[17]));
        } else {
            BigDecimal valorChamado   = toBD(r[27]);
            BigDecimal valorKm        = toBD(r[28]);
            BigDecimal km             = toBD(r[29]);
            BigDecimal pedagio        = toBD(r[30]);
            BigDecimal estacionamento = toBD(r[31]);
            BigDecimal valorOutros    = toBD(r[32]);

            BigDecimal valorTotal = valorChamado
                    .add(km.multiply(valorKm))
                    .add(pedagio)
                    .add(estacionamento)
                    .add(valorOutros);

            o.setValorChamado(valorChamado);
            o.setKm(km);
            o.setValorKm(valorKm);
            o.setPedagio(pedagio);
            o.setEstacionamento(estacionamento);
            o.setValorOutros(valorOutros);
            o.setValorTotal(valorTotal);
        }

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
}