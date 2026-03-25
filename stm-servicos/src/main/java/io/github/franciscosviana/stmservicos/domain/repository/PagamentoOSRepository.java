package io.github.franciscosviana.stmservicos.domain.repository;

import io.github.franciscosviana.stmservicos.domain.model.PagamentoOS;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PagamentoOSRepository extends JpaRepository<PagamentoOS, Long> {

    Optional<PagamentoOS> findByOrdemServicoId(UUID ordemServicoId);

    boolean existsByOrdemServicoId(UUID ordemServicoId);
}