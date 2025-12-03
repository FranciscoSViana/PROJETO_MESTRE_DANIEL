package io.github.franciscosviana.stmservicos.domain.repository;

import io.github.franciscosviana.stmservicos.domain.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario,Long> {

    Optional<Usuario> findByNome(String nome);
    Optional<Usuario> findByEmail(String email);
    boolean existsByNome(String nome);
    boolean existsByEmail(String email);
}
