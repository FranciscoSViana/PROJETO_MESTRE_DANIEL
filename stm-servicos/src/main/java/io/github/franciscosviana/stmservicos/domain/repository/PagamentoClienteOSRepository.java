package io.github.franciscosviana.stmservicos.domain.repository;

import io.github.franciscosviana.stmservicos.domain.model.PagamentoClienteOS;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PagamentoClienteOSRepository extends JpaRepository<PagamentoClienteOS, Long> {

    Optional<PagamentoClienteOS> findByOrdemServicoId(UUID ordemServicoId);

    boolean existsByOrdemServicoId(UUID ordemServicoId);
}