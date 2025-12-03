package io.github.franciscosviana.stmservicos.domain.repository;

import io.github.franciscosviana.stmservicos.domain.model.SenhaResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface SenhaResetTokenRepository extends JpaRepository<SenhaResetToken, Long> {

    Optional<SenhaResetToken> findByToken(String token);
    void deleteByExpiryDateBefore(Instant now);
}
