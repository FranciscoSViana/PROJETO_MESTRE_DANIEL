package io.github.franciscosviana.stmservicos.domain.repository;

import io.github.franciscosviana.stmservicos.domain.model.OrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, UUID> {

    @Query("SELECT o.osg FROM OrdemServico o ORDER BY o.id DESC")
    String findLastOsg();
}
