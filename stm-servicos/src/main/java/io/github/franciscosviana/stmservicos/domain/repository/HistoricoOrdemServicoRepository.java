package io.github.franciscosviana.stmservicos.domain.repository;

import io.github.franciscosviana.stmservicos.domain.model.HistoricoOrdemServico;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

@Transactional
public interface HistoricoOrdemServicoRepository extends JpaRepository<HistoricoOrdemServico, UUID> {

    List<HistoricoOrdemServico> findByOrdemServicoIdOrderByDataHoraAsc(UUID ordemServicoId);

    @Modifying
    @Query("DELETE FROM HistoricoOrdemServico h WHERE h.ordemServico.id = :ordemServicoId")
    void deleteByOrdemServicoId(UUID ordemServicoId);
}

