package io.github.franciscosviana.stmservicos.domain.repository;

import io.github.franciscosviana.stmservicos.domain.model.Tecnico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface TecnicoRepository extends JpaRepository<Tecnico, UUID> {

    Page<Tecnico> findByCredenciadoId(UUID credenciadoId, Pageable pageable);

    @Query("select max(t.codigo) from Tecnico t")
    Long findMaxCodigo();
}
