package io.github.franciscosviana.stmservicos.domain.repository.spec;

import io.github.franciscosviana.stmservicos.domain.model.OrdemServico;
import io.github.franciscosviana.stmservicos.domain.model.enums.StatusOrdem;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class OrdemServicoSpecification {

    private static final Logger log = LoggerFactory.getLogger(OrdemServicoSpecification.class);

    public static Specification<OrdemServico> filtro(
            String osClt,
            String osg,
            String dataAbertura,
            String status,
            String cliente,
            String credenciado,
            String cidade,
            String estado,
            String rastreio
    ) {

        // ✅ LOG: mostra exatamente o que chegou do frontend
        log.info("=== OrdemServicoSpecification.filtro ===");
        log.info("  osClt       : [{}]", osClt);
        log.info("  osg         : [{}]", osg);
        log.info("  dataAbertura: [{}] (length={})", dataAbertura,
                dataAbertura != null ? dataAbertura.length() : "null");
        log.info("  status      : [{}]", status);
        log.info("  cliente     : [{}]", cliente);
        log.info("  credenciado : [{}]", credenciado);
        log.info("  cidade      : [{}]", cidade);
        log.info("  estado      : [{}]", estado);
        log.info("  rastreio    : [{}]", rastreio);
        log.info("========================================");

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // ── osClt ──────────────────────────────────────────────────────
            if (osClt != null) {
                log.debug("Aplicando filtro osClt: {}", osClt);
                predicates.add(cb.like(cb.lower(root.get("osClt")),
                        "%" + osClt.toLowerCase() + "%"));
            }

            // ── osg ────────────────────────────────────────────────────────
            if (osg != null) {
                log.debug("Aplicando filtro osg: {}", osg);
                predicates.add(cb.like(cb.lower(root.get("osg")),
                        "%" + osg.toLowerCase() + "%"));
            }

            // ── dataAbertura ───────────────────────────────────────────────
            if (dataAbertura != null && !dataAbertura.isBlank()) {
                log.info("Tentando parsear dataAbertura: [{}]", dataAbertura);
                LocalDate data = parsearData(dataAbertura);
                if (data != null) {
                    log.info("Data parseada com sucesso: {}", data);
                    predicates.add(
                            cb.equal(
                                    cb.function("date", java.sql.Date.class, root.get("dataHoraAbertura")),
                                    java.sql.Date.valueOf(data)
                            )
                    );
                } else {
                    log.warn("dataAbertura [{}] não pôde ser parseada — filtro de data IGNORADO", dataAbertura);
                }
            }

            // ── status ─────────────────────────────────────────────────────
            if (status != null) {
                log.debug("Aplicando filtro status: {}", status);
                try {
                    StatusOrdem statusEnum = StatusOrdem.valueOf(status.toUpperCase());
                    predicates.add(cb.equal(root.get("status"), statusEnum));
                } catch (IllegalArgumentException e) {
                    log.warn("Status inválido recebido: [{}] — valores válidos: {} — filtro IGNORADO",
                            status, List.of(StatusOrdem.values()));
                }
            }

            // ── cliente ────────────────────────────────────────────────────
            if (cliente != null) {
                log.debug("Aplicando filtro cliente: {}", cliente);
                predicates.add(cb.like(
                        cb.lower(root.join("cliente").get("nome")),
                        "%" + cliente.toLowerCase() + "%"));
            }

            // ── credenciado ────────────────────────────────────────────────
            if (credenciado != null) {
                log.debug("Aplicando filtro credenciado: {}", credenciado);
                predicates.add(cb.like(
                        cb.lower(root.join("credenciado").get("rag")),
                        "%" + credenciado.toLowerCase() + "%"));
            }

            // ── cidade ─────────────────────────────────────────────────────
            if (cidade != null) {
                log.debug("Aplicando filtro cidade: {}", cidade);
                predicates.add(cb.like(
                        cb.lower(root.get("endereco").get("cidade")),
                        "%" + cidade.toLowerCase() + "%"));
            }

            // ── estado ─────────────────────────────────────────────────────
            if (estado != null) {
                log.debug("Aplicando filtro estado: {}", estado);
                predicates.add(cb.like(
                        cb.lower(root.get("endereco").get("estado")),
                        "%" + estado.toLowerCase() + "%"));
            }

            // ── rastreio ───────────────────────────────────────────────────
            if (rastreio != null) {
                log.debug("Aplicando filtro rastreio: {}", rastreio);
                predicates.add(cb.like(
                        cb.lower(root.get("rastreio")),
                        "%" + rastreio.toLowerCase() + "%"));
            }

            log.info("Total de predicates aplicados: {}", predicates.size());

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Tenta parsear a data nos formatos abaixo, nesta ordem de prioridade:
     * <p>
     * 1. yyyy-MM-dd  → padrão ISO enviado pelo Angular após conversão no TS
     * 2. dd/MM/yyyy  → formato BR com barras
     * 3. ddMMyyyy    → formato BR sem separador (8 dígitos)
     * <p>
     * Retorna null para qualquer entrada inválida ou incompleta.
     */
    private static LocalDate parsearData(String valor) {
        if (valor == null || valor.isBlank()) return null;

        log.debug("parsearData recebeu: [{}]", valor);

        // 1. ISO: yyyy-MM-dd  (ex: "2025-03-10") — padrão enviado pelo Angular
        if (valor.matches("\\d{4}-\\d{2}-\\d{2}")) {
            try {
                LocalDate result = LocalDate.parse(valor, DateTimeFormatter.ISO_LOCAL_DATE);
                log.debug("parsearData: formato ISO detectado → {}", result);
                return result;
            } catch (DateTimeParseException e) {
                log.warn("parsearData: falhou no formato ISO para [{}]: {}", valor, e.getMessage());
            }
        }

        // 2. BR com barras: dd/MM/yyyy  (ex: "10/03/2025")
        if (valor.matches("\\d{2}/\\d{2}/\\d{4}")) {
            try {
                LocalDate result = LocalDate.parse(valor, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                log.debug("parsearData: formato BR com barras detectado → {}", result);
                return result;
            } catch (DateTimeParseException e) {
                log.warn("parsearData: falhou no formato BR/barras para [{}]: {}", valor, e.getMessage());
            }
        }

        // 3. BR sem separador: ddMMyyyy  (ex: "10032025")
        if (valor.matches("\\d{8}")) {
            try {
                LocalDate result = LocalDate.parse(valor, DateTimeFormatter.ofPattern("ddMMyyyy"));
                log.debug("parsearData: formato BR sem barras detectado → {}", result);
                return result;
            } catch (DateTimeParseException e) {
                log.warn("parsearData: falhou no formato ddMMyyyy para [{}]: {}", valor, e.getMessage());
            }
        }

        log.warn("parsearData: nenhum formato reconhecido para [{}] — retornando null", valor);
        return null;
    }
}