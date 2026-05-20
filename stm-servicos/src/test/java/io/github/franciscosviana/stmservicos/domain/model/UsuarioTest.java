package io.github.franciscosviana.stmservicos.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Usuario")
class UsuarioTest {

    @Nested
    @DisplayName("getIdade()")
    class GetIdade {

        @Test
        @DisplayName("deve calcular corretamente a partir da data de nascimento")
        void deveCalcularIdade() {
            LocalDate nascimento = LocalDate.now().minusYears(30);
            Usuario u = usuario().dataNascimento(nascimento).build();
            assertThat(u.getIdade()).isEqualTo(30);
        }

        @Test
        @DisplayName("deve retornar null quando dataNascimento é nula")
        void deveRetornarNullSemNascimento() {
            Usuario u = usuario().dataNascimento(null).build();
            assertThat(u.getIdade()).isNull();
        }
    }

    @Nested
    @DisplayName("flags padrão")
    class FlagsPadrao {

        @Test
        @DisplayName("enabled deve ser true por padrão no new Usuario()")
        void enabledPadrao() {
            assertThat(new Usuario().isEnabled()).isTrue();
        }

        @Test
        @DisplayName("notificacaoSenhaEnviada deve ser false por padrão")
        void notificacaoPadrao() {
            assertThat(new Usuario().isNotificacaoSenhaEnviada()).isFalse();
        }
    }

    @Nested
    @DisplayName("builder")
    class Builder {

        @Test
        @DisplayName("deve criar usuário com todos os campos corretamente")
        void builderCompleto() {
            UUID id = UUID.randomUUID();
            Usuario u = Usuario.builder()
                    .id(id)
                    .nomeCompleto("João Silva")
                    .username("joao.silva")
                    .nome("joao.silva")
                    .email("joao@email.com")
                    .senha("hash")
                    .roles(Set.of("USER", "ADMIN"))
                    .enabled(true)
                    .createdAt(Instant.now())
                    .ultimaAlteracaoSenha(Instant.now())
                    .build();

            assertThat(u.getId()).isEqualTo(id);
            assertThat(u.getNomeCompleto()).isEqualTo("João Silva");
            assertThat(u.getUsername()).isEqualTo("joao.silva");
            assertThat(u.getRoles()).containsExactlyInAnyOrder("USER", "ADMIN");
            assertThat(u.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("username e nome devem ser iguais quando gerados automaticamente")
        void usernameENomaIguais() {
            Usuario u = usuario().username("francisco.viana").nome("francisco.viana").build();
            assertThat(u.getUsername()).isEqualTo(u.getNome());
        }
    }

    private Usuario.UsuarioBuilder usuario() {
        return Usuario.builder()
                .username("joao").nome("joao").email("j@e.com").senha("h")
                .roles(Set.of("USER")).createdAt(Instant.now())
                .ultimaAlteracaoSenha(Instant.now());
    }
}
