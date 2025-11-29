package io.github.franciscosviana.stmservicos.domain.repository;

import io.github.franciscosviana.stmservicos.domain.model.OrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {
}
