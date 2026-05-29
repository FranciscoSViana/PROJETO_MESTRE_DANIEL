package io.github.franciscosviana.stmservicos.domain.repository.spec;

import io.github.franciscosviana.stmservicos.domain.model.OrdemServico;
import io.github.franciscosviana.stmservicos.domain.model.enums.StatusOrdem;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class OrdemServicoSpecification {

    public static Specification<OrdemServico> filtro(
            String osClt, String osg, String dataAbertura, String status,
            String cliente, String credenciado, String cidade, String estado, String rastreio
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (osClt != null)
                predicates.add(cb.like(cb.lower(root.get("osClt")), "%" + osClt.toLowerCase() + "%"));

            if (osg != null)
                predicates.add(cb.like(cb.lower(root.get("osg")), "%" + osg.toLowerCase() + "%"));

            if (dataAbertura != null && !dataAbertura.isBlank()) {
                LocalDate data = parsearData(dataAbertura);
                if (data != null)
                    predicates.add(cb.equal(
                            cb.function("date", java.sql.Date.class, root.get("dataHoraAbertura")),
                            java.sql.Date.valueOf(data)));
            }

            if (status != null) {
                try {
                    predicates.add(cb.equal(root.get("status"), StatusOrdem.valueOf(status.toUpperCase())));
                } catch (IllegalArgumentException ignored) {
                    log.info(ignored.getMessage());
                }
            }

            if (cliente != null)
                predicates.add(cb.like(cb.lower(root.join("cliente").get("nome")), "%" + cliente.toLowerCase() + "%"));

            if (credenciado != null)
                predicates.add(cb.like(cb.lower(root.join("credenciado").get("rag")), "%" + credenciado.toLowerCase() + "%"));

            if (cidade != null)
                predicates.add(cb.like(cb.lower(root.get("endereco").get("cidade")), "%" + cidade.toLowerCase() + "%"));

            if (estado != null)
                predicates.add(cb.like(cb.lower(root.get("endereco").get("estado")), "%" + estado.toLowerCase() + "%"));

            if (rastreio != null)
                predicates.add(cb.like(cb.lower(root.get("rastreio")), "%" + rastreio.toLowerCase() + "%"));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static LocalDate parsearData(String valor) {
        if (valor == null || valor.isBlank()) return null;

        if (valor.matches("\\d{4}-\\d{2}-\\d{2}")) {
            try {
                return LocalDate.parse(valor, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException ignored) {
                log.info(ignored.getMessage());
            }
        }
        if (valor.matches("\\d{2}/\\d{2}/\\d{4}")) {
            try {
                return LocalDate.parse(valor, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (DateTimeParseException ignored) {
                log.info(ignored.getMessage());
            }
        }
        if (valor.matches("\\d{8}")) {
            try {
                return LocalDate.parse(valor, DateTimeFormatter.ofPattern("ddMMyyyy"));
            } catch (DateTimeParseException ignored) {
                log.info(ignored.getMessage());
            }
        }
        return null;
    }
}