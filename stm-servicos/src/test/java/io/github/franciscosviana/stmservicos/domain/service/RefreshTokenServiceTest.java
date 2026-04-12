package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.domain.model.RefreshToken;
import io.github.franciscosviana.stmservicos.domain.model.Usuario;
import io.github.franciscosviana.stmservicos.domain.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService")
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService service;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .username("joao.silva")
                .nome("joao.silva")
                .email("joao@email.com")
                .senha("hash")
                .roles(Set.of("USER"))
                .createdAt(Instant.now())
                .ultimaAlteracaoSenha(Instant.now())
                .build();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // create()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("deve criar RefreshToken com token UUID e expiração em 30 dias")
        void deveCriarToken() {
            when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RefreshToken resultado = service.create(usuario);

            assertThat(resultado.getToken()).isNotBlank();
            assertThat(resultado.getUsuario()).isEqualTo(usuario);
            assertThat(resultado.getExpiryDate())
                    .isAfter(Instant.now().plus(29, ChronoUnit.DAYS))
                    .isBefore(Instant.now().plus(31, ChronoUnit.DAYS));
        }

        @Test
        @DisplayName("deve gerar tokens únicos a cada chamada")
        void deveGerarTokensUnicos() {
            when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            RefreshToken t1 = service.create(usuario);
            RefreshToken t2 = service.create(usuario);

            assertThat(t1.getToken()).isNotEqualTo(t2.getToken());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // validate()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("validate()")
    class Validate {

        @Test
        @DisplayName("deve retornar RefreshToken quando válido e não expirado")
        void deveRetornarTokenValido() {
            RefreshToken token = RefreshToken.builder()
                    .token("token-valido")
                    .usuario(usuario)
                    .expiryDate(Instant.now().plus(1, ChronoUnit.DAYS))
                    .build();

            when(refreshTokenRepository.findByToken("token-valido")).thenReturn(Optional.of(token));

            assertThat(service.validate("token-valido")).isEqualTo(token);
        }

        @Test
        @DisplayName("deve lançar RuntimeException quando token não encontrado")
        void deveLancarExcecaoSeTokenInvalido() {
            when(refreshTokenRepository.findByToken("token-invalido")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.validate("token-invalido"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("inválido");
        }

        @Test
        @DisplayName("deve lançar RuntimeException quando token expirado")
        void deveLancarExcecaoSeTokenExpirado() {
            RefreshToken tokenExpirado = RefreshToken.builder()
                    .token("token-expirado")
                    .usuario(usuario)
                    .expiryDate(Instant.now().minus(1, ChronoUnit.HOURS))
                    .build();

            when(refreshTokenRepository.findByToken("token-expirado"))
                    .thenReturn(Optional.of(tokenExpirado));

            assertThatThrownBy(() -> service.validate("token-expirado"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("expirado");
        }
    }
}
