package io.github.franciscosviana.stmservicos.domain.service.helper;

import io.github.franciscosviana.stmservicos.common.auth.AdminInitializer;
import io.github.franciscosviana.stmservicos.domain.model.Usuario;
import io.github.franciscosviana.stmservicos.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("UsuarioAutenticadoService e AdminInitializer")
class UsuarioAutenticadoAndAdminInitializerTest {

    // ──────────────────────────────────────────────────────────────────────────
    // UsuarioAutenticadoService
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("UsuarioAutenticadoService")
    @ExtendWith(MockitoExtension.class)
    class UsuarioAutenticadoServiceTest {

        @Mock UsuarioRepository usuarioRepository;
        @InjectMocks UsuarioAutenticadoService service;

        @BeforeEach
        void setarAutenticacao() {
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken("joao.silva", null, Set.of());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        @AfterEach
        void limparContexto() {
            SecurityContextHolder.clearContext();
        }

        @Test
        @DisplayName("getUsuarioLogado() deve retornar usuário do SecurityContext")
        void deveRetornarUsuarioLogado() {
            Usuario usuario = Usuario.builder()
                    .username("joao.silva").nome("joao.silva")
                    .email("j@e.com").senha("h")
                    .roles(Set.of("USER")).enabled(true)
                    .createdAt(Instant.now()).ultimaAlteracaoSenha(Instant.now()).build();

            when(usuarioRepository.findByNome("joao.silva")).thenReturn(Optional.of(usuario));

            Usuario result = service.getUsuarioLogado();

            assertThat(result.getUsername()).isEqualTo("joao.silva");
        }

        @Test
        @DisplayName("getUsuarioLogado() deve lançar RuntimeException quando usuário não encontrado")
        void deveLancarExcecaoSeNaoEncontrado() {
            when(usuarioRepository.findByNome("joao.silva")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getUsuarioLogado())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("não encontrado");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // AdminInitializer
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("AdminInitializer")
    @ExtendWith(MockitoExtension.class)
    class AdminInitializerTest {

        @Mock PasswordEncoder passwordEncoder;
        @Mock UsuarioRepository usuarioRepository;
        @InjectMocks AdminInitializer adminInitializer;

        @Test
        @DisplayName("run() deve criar admin quando ele não existe")
        void deveCriarAdminQuandoNaoExiste() throws Exception {
            when(usuarioRepository.existsByNome("admin")).thenReturn(false);
            when(passwordEncoder.encode("admin")).thenReturn("$2a$hash-admin");

            adminInitializer.run();

            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
            verify(usuarioRepository).save(captor.capture());

            Usuario salvo = captor.getValue();
            assertThat(salvo.getNome()).isEqualTo("admin");
            assertThat(salvo.getEmail()).isEqualTo("admin@sistema.com");
            assertThat(salvo.getSenha()).isEqualTo("$2a$hash-admin");
            assertThat(salvo.getRoles()).contains("ADMIN");
            assertThat(salvo.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("run() não deve criar admin quando ele já existe")
        void naoDeveCriarAdminSeJaExiste() throws Exception {
            when(usuarioRepository.existsByNome("admin")).thenReturn(true);

            adminInitializer.run();

            verify(usuarioRepository, never()).save(any());
        }
    }
}
