package io.github.franciscosviana.stmservicos.domain.repository;

import io.github.franciscosviana.stmservicos.domain.model.SenhaResetToken;
import io.github.franciscosviana.stmservicos.domain.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface SenhaResetTokenRepository extends JpaRepository<SenhaResetToken, Long> {

    void deleteByUsuario(Usuario usuario);
    void deleteByExpiryDateBefore(Instant now);
    Optional<SenhaResetToken> findByToken(String token);
}
