package io.github.franciscosviana.stmservicos.domain.repository;

import io.github.franciscosviana.stmservicos.domain.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByNome(String nome);

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByEmail(String email);

    boolean existsByNome(String nome);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    /**
     * Busca todos os usernames que começam com o prefixo informado.
     * Usado para gerar usernames únicos (ex: francisco.viana, francisco.viana2...)
     */
    List<Usuario> findByUsernameStartingWith(String prefix);
}