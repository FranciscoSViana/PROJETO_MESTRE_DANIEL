package io.github.franciscosviana.stmservicos.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RefreshToken")
class RefreshTokenTest {

    @Test
    @DisplayName("builder deve criar RefreshToken com token e expiração corretos")
    void builderFunciona() {
        Instant expiry = Instant.now().plusSeconds(3600);
        RefreshToken t = RefreshToken.builder()
                .token("abc-123")
                .expiryDate(expiry)
                .build();

        assertThat(t.getToken()).isEqualTo("abc-123");
        assertThat(t.getExpiryDate()).isEqualTo(expiry);
    }

    @Test
    @DisplayName("token deve ser único por construção")
    void tokenNaoNulo() {
        RefreshToken t = RefreshToken.builder()
                .token("unique-token-xyz")
                .expiryDate(Instant.now().plusSeconds(100))
                .build();

        assertThat(t.getToken()).isNotBlank();
    }

    @Test
    @DisplayName("expiryDate deve estar no futuro para token válido")
    void expiryDateFuturo() {
        Instant futuro = Instant.now().plusSeconds(1800);
        RefreshToken t = RefreshToken.builder()
                .token("token")
                .expiryDate(futuro)
                .build();

        assertThat(t.getExpiryDate()).isAfter(Instant.now());
    }
}
