package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.domain.model.Usuario;
import io.github.franciscosviana.stmservicos.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@DisplayName("UsuarioDetailsService")
@ExtendWith(MockitoExtension.class)
class UsuarioDetailsServiceTest {

    @Mock
    UsuarioRepository usuarioRepository;

    @InjectMocks
    UsuarioDetailsService service;

    @Nested
    @DisplayName("loadUserByUsername()")
    class LoadUserByUsername {

        @Test
        @DisplayName("deve retornar UserDetails quando usuário encontrado")
        void deveCarregarUsuario() {
            Usuario usuario = Usuario.builder()
                    .nome("joao.silva")
                    .username("joao.silva")
                    .senha("$2a$hash")
                    .roles(Set.of("USER"))
                    .enabled(true)
                    .createdAt(Instant.now())
                    .ultimaAlteracaoSenha(Instant.now())
                    .build();

            when(usuarioRepository.findByNome("joao.silva")).thenReturn(Optional.of(usuario));

            UserDetails details = service.loadUserByUsername("joao.silva");

            assertThat(details.getUsername()).isEqualTo("joao.silva");
            assertThat(details.getPassword()).isEqualTo("$2a$hash");
            assertThat(details.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("deve lançar UsernameNotFoundException quando usuário não encontrado")
        void deveLancarExcecaoSeNaoEncontrado() {
            when(usuarioRepository.findByNome("inexistente")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.loadUserByUsername("inexistente"))
                    .isInstanceOf(UsernameNotFoundException.class);
        }

        @Test
        @DisplayName("deve retornar authorities com prefixo ROLE_")
        void deveRetornarRolesComPrefixo() {
            Usuario usuario = Usuario.builder()
                    .nome("admin")
                    .username("admin")
                    .senha("hash")
                    .roles(Set.of("ADMIN"))
                    .enabled(true)
                    .createdAt(Instant.now())
                    .ultimaAlteracaoSenha(Instant.now())
                    .build();

            when(usuarioRepository.findByNome("admin")).thenReturn(Optional.of(usuario));

            UserDetails details = service.loadUserByUsername("admin");

            assertThat(details.getAuthorities())
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        }

        @Test
        @DisplayName("deve retornar múltiplas authorities para múltiplos roles")
        void deveRetornarMultiplasRoles() {
            Usuario usuario = Usuario.builder()
                    .nome("superuser")
                    .username("superuser")
                    .senha("hash")
                    .roles(Set.of("ADMIN", "USER"))
                    .enabled(true)
                    .createdAt(Instant.now())
                    .ultimaAlteracaoSenha(Instant.now())
                    .build();

            when(usuarioRepository.findByNome("superuser")).thenReturn(Optional.of(usuario));

            UserDetails details = service.loadUserByUsername("superuser");

            assertThat(details.getAuthorities()).hasSize(2);
        }
    }
}
