package io.github.franciscosviana.stmservicos.domain.repository;

import io.github.franciscosviana.stmservicos.domain.model.HistoricoOrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HistoricoOrdemServicoRepository extends JpaRepository<HistoricoOrdemServico, UUID> {

    List<HistoricoOrdemServico> findByOrdemServicoIdOrderByDataHoraAsc(UUID ordemServicoId);
}

