package io.github.franciscosviana.stmservicos.domain.repository;

import io.github.franciscosviana.stmservicos.domain.model.OrdemServico;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, UUID>, JpaSpecificationExecutor<OrdemServico> {

    @Query("""
                SELECT o.osg
                FROM OrdemServico o
                WHERE o.osg LIKE 'OSG%'
                ORDER BY o.osg DESC
            """)
    List<String> findLastOsg(PageRequest pageRequest);

    @Query("""
                select os from OrdemServico os
                join fetch os.cliente c
                left join fetch c.contratos
                left join fetch os.contrato
                left join fetch os.credenciado
                left join fetch os.tecnico
                where os.id = :id
            """)
    Optional<OrdemServico> buscarCompleta(@Param("id") UUID id);
}
