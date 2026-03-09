package io.github.franciscosviana.stmservicos.domain.repository.spec;

import io.github.franciscosviana.stmservicos.domain.model.Credenciado;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class CredenciadoSpecification {

    public static Specification<Credenciado> comFiltros(
            Long codigo, String rag, String numeroPessoa,
            String cidade, String estado) {

        return (root, query, cb) -> {

            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("endereco", JoinType.LEFT);
            }

            // ✅ Usar List<Predicate> em vez de cb.conjunction()
            List<Predicate> predicates = new ArrayList<>();

            if (codigo != null) {
                predicates.add(cb.equal(root.get("codigo"), codigo));
            }
            if (rag != null && !rag.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("rag")), "%" + rag.toLowerCase() + "%"));
            }
            if (numeroPessoa != null && !numeroPessoa.isBlank()) {
                predicates.add(cb.like(root.get("numeroPessoa"), "%" + numeroPessoa + "%"));
            }
            if (cidade != null && !cidade.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("endereco").get("cidade")),
                        "%" + cidade.toLowerCase() + "%"));
            }
            if (estado != null && !estado.isBlank()) {
                predicates.add(cb.equal(root.get("endereco").get("estado"), estado));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
