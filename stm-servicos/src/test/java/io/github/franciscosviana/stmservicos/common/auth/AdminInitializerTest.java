package io.github.franciscosviana.stmservicos.common.auth;

import io.github.franciscosviana.stmservicos.domain.model.Usuario;
import io.github.franciscosviana.stmservicos.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("AdminInitializer")
@ExtendWith(MockitoExtension.class)
class AdminInitializerTest {

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    UsuarioRepository usuarioRepository;

    @InjectMocks
    AdminInitializer adminInitializer;

    @Nested
    @DisplayName("run()")
    class Run {

        @Test
        @DisplayName("deve criar admin quando ele não existe")
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
        @DisplayName("não deve criar admin quando ele já existe")
        void naoDeveCriarAdminSeJaExiste() throws Exception {
            when(usuarioRepository.existsByNome("admin")).thenReturn(true);

            adminInitializer.run();

            verify(usuarioRepository, never()).save(any());
        }
    }
}
