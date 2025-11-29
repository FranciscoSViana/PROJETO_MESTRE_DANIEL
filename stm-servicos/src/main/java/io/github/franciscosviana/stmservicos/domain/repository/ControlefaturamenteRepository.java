package io.github.franciscosviana.stmservicos.domain.repository;

import io.github.franciscosviana.stmservicos.domain.model.ControleFaturamento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ControlefaturamenteRepository extends JpaRepository<ControleFaturamento, Long> {
}
