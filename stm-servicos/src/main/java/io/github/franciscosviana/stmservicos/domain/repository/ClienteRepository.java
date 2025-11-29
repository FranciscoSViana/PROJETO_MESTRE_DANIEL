package io.github.franciscosviana.stmservicos.domain.repository;

import io.github.franciscosviana.stmservicos.domain.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {

    @Query("SELECT COALESCE(MAX(c.codigo), 0) FROM Cliente c")
    Long buscarUltimoCodigo();
}
