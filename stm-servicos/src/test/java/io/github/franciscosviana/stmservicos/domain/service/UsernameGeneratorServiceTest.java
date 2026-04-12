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

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsernameGeneratorService")
class UsernameGeneratorServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsernameGeneratorService service;

    @Nested
    @DisplayName("gerar() — formatos básicos")
    class GerarFormatos {

        @Test
        @DisplayName("deve gerar 'primeiro.ultimo' para nome com 2 partes")
        void deveGerarPrimeiroUltimo() {
            when(usuarioRepository.findByUsernameStartingWith("francisco")).thenReturn(Collections.emptyList());

            String username = service.gerar("Francisco Viana");

            assertThat(username).isEqualTo("francisco.viana");
        }

        @Test
        @DisplayName("deve gerar apenas o primeiro nome quando nome tem 1 parte")
        void deveGerarApenasUmNome() {
            when(usuarioRepository.findByUsernameStartingWith("francisco")).thenReturn(Collections.emptyList());

            String username = service.gerar("Francisco");

            assertThat(username).isEqualTo("francisco");
        }

        @Test
        @DisplayName("deve remover acentos e normalizar para minúsculas")
        void deveRemoverAcentos() {
            when(usuarioRepository.findByUsernameStartingWith("joao")).thenReturn(Collections.emptyList());

            String username = service.gerar("João Conceição");

            assertThat(username).isEqualTo("joao.conceicao");
        }

        @Test
        @DisplayName("deve tratar espaços múltiplos no nome")
        void deveTratarEspacosMultiplos() {
            when(usuarioRepository.findByUsernameStartingWith("ana")).thenReturn(Collections.emptyList());

            String username = service.gerar("  Ana   Paula  ");

            assertThat(username).isEqualTo("ana.paula");
        }
    }

    @Nested
    @DisplayName("gerar() — colisões e fallbacks")
    class GerarColisoes {

        @Test
        @DisplayName("deve usar 'primeiro.inicial.ultimo' quando 'primeiro.ultimo' já existe")
        void deveUsarInicialMeio() {
            Usuario existente = usuarioComUsername("francisco.viana");
            when(usuarioRepository.findByUsernameStartingWith("francisco"))
                    .thenReturn(List.of(existente));

            String username = service.gerar("Francisco Santos Viana");

            assertThat(username).isEqualTo("francisco.s.viana");
        }

        @Test
        @DisplayName("deve usar 'primeiro.nomeMeio.ultimo' quando os dois anteriores já existem")
        void deveUsarNomeMeio() {
            Usuario u1 = usuarioComUsername("francisco.viana");
            Usuario u2 = usuarioComUsername("francisco.s.viana");
            when(usuarioRepository.findByUsernameStartingWith("francisco"))
                    .thenReturn(List.of(u1, u2));

            String username = service.gerar("Francisco Santos Viana");

            assertThat(username).isEqualTo("francisco.santos.viana");
        }

        @Test
        @DisplayName("deve usar sufixo numérico quando todos os padrões nomeados já existem")
        void deveUsarSufixoNumerico() {
            List<Usuario> existentes = List.of(
                    usuarioComUsername("francisco.viana"),
                    usuarioComUsername("francisco.s.viana"),
                    usuarioComUsername("francisco.santos.viana")
            );
            when(usuarioRepository.findByUsernameStartingWith("francisco")).thenReturn(existentes);
            when(usuarioRepository.existsByUsername("francisco.viana2")).thenReturn(false);

            String username = service.gerar("Francisco Santos Viana");

            assertThat(username).isEqualTo("francisco.viana2");
        }

        @Test
        @DisplayName("deve incrementar sufixo até encontrar disponível")
        void deveIncrementarSufixo() {
            List<Usuario> existentes = List.of(
                    usuarioComUsername("francisco.viana"),
                    usuarioComUsername("francisco.s.viana"),
                    usuarioComUsername("francisco.santos.viana")
            );
            when(usuarioRepository.findByUsernameStartingWith("francisco")).thenReturn(existentes);
            when(usuarioRepository.existsByUsername("francisco.viana2")).thenReturn(true);
            when(usuarioRepository.existsByUsername("francisco.viana3")).thenReturn(true);
            when(usuarioRepository.existsByUsername("francisco.viana4")).thenReturn(false);

            String username = service.gerar("Francisco Santos Viana");

            assertThat(username).isEqualTo("francisco.viana4");
        }
    }

    @Nested
    @DisplayName("gerar() — entradas inválidas")
    class GerarInvalidos {

        @Test
        @DisplayName("deve lançar IllegalArgumentException para nome nulo")
        void deveLancarExcecaoParaNomeNulo() {
            assertThatThrownBy(() -> service.gerar(null))
                    .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("deve lançar IllegalArgumentException para nome vazio")
        void deveLancarExcecaoParaNomeVazio() {

            assertThatThrownBy(() -> service.gerar("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Nome inválido");
        }
    }

    private Usuario usuarioComUsername(String username) {
        return Usuario.builder()
                .username(username)
                .nome(username)
                .email(username + "@test.com")
                .senha("hash")
                .roles(Set.of("USER"))
                .build();
    }
}
