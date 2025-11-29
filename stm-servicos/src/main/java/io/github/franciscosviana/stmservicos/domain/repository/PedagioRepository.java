package io.github.franciscosviana.stmservicos.domain.repository;

import io.github.franciscosviana.stmservicos.domain.model.Pedagio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedagioRepository extends JpaRepository<Pedagio, Long> {
}
