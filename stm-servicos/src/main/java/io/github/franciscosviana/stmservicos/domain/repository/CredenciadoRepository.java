package io.github.franciscosviana.stmservicos.domain.repository;

import io.github.franciscosviana.stmservicos.domain.model.Credenciado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface CredenciadoRepository extends JpaRepository<Credenciado, UUID> {

    @Query("SELECT COALESCE(MAX(c.codigo), 0) FROM Credenciado c")
    Long buscarUltimoCodigo();
}
