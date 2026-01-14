package io.github.franciscosviana.stmservicos.domain.repository;

import io.github.franciscosviana.stmservicos.domain.model.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ContratoRepository extends JpaRepository<Contrato, UUID> {

    List<Contrato> findByClienteId(UUID clienteId);

    Optional<Contrato> findByIdAndClienteId(UUID id, UUID clienteId);
}

