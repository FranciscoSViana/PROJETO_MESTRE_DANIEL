package io.github.franciscosviana.stmservicos.domain.repository;

import io.github.franciscosviana.stmservicos.domain.model.Cidade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CidadeRespository extends JpaRepository<Cidade, Long> {
}
