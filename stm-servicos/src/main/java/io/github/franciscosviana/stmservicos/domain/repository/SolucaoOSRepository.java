package io.github.franciscosviana.stmservicos.domain.repository;

import io.github.franciscosviana.stmservicos.domain.model.SolucaoOS;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SolucaoOSRepository extends JpaRepository<SolucaoOS, Long> {

    Optional<SolucaoOS> findByOrdemServicoId(UUID ordemId);

}
