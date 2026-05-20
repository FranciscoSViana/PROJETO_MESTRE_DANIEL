package io.github.franciscosviana.stmservicos.domain.service.helper;

import io.github.franciscosviana.stmservicos.domain.model.Usuario;
import io.github.franciscosviana.stmservicos.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@DisplayName("UsuarioAutenticadoService")
@ExtendWith(MockitoExtension.class)
class UsuarioAutenticadoServiceTest {

    @Mock
    UsuarioRepository usuarioRepository;

    @InjectMocks
    UsuarioAutenticadoService service;

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

    @Nested
    @DisplayName("getUsuarioLogado()")
    class GetUsuarioLogado {

        @Test
        @DisplayName("deve retornar usuário do SecurityContext quando encontrado")
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
        @DisplayName("deve lançar RuntimeException quando usuário não encontrado no repositório")
        void deveLancarExcecaoSeNaoEncontrado() {
            when(usuarioRepository.findByNome("joao.silva")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getUsuarioLogado())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("não encontrado");
        }
    }
}
