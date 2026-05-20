package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.domain.model.NotificacaoUsuario;
import io.github.franciscosviana.stmservicos.domain.model.Usuario;
import io.github.franciscosviana.stmservicos.domain.repository.NotificacaoUsuarioRepository;
import io.github.franciscosviana.stmservicos.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("NotificacaoSenhaService")
@ExtendWith(MockitoExtension.class)
class NotificacaoSenhaServiceTest {

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    NotificacaoUsuarioRepository notificacaoRepository;

    @Mock
    EmailService emailService;

    @InjectMocks
    NotificacaoSenhaService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "frontendUrl", "http://localhost:4200");
    }

    @Nested
    @DisplayName("registrarTrocaSenha()")
    class RegistrarTrocaSenha {

        @Test
        @DisplayName("deve atualizar ultimaAlteracaoSenha e zerar flag de notificação")
        void deveRegistrarTrocaSenha() {
            Usuario usuario = usuarioComSenhaAntiga(100);
            usuario.setNotificacaoSenhaEnviada(true);

            service.registrarTrocaSenha(usuario);

            assertThat(usuario.isNotificacaoSenhaEnviada()).isFalse();
            assertThat(usuario.getUltimaAlteracaoSenha())
                    .isAfter(Instant.now().minus(5, ChronoUnit.SECONDS));
            verify(usuarioRepository).save(usuario);
        }

        @Test
        @DisplayName("deve salvar usuário com nova data de alteração de senha")
        void deveSalvarComNovaData() {
            Usuario usuario = usuarioComSenhaAntiga(50);

            service.registrarTrocaSenha(usuario);

            verify(usuarioRepository).save(usuario);
        }
    }

    @Nested
    @DisplayName("verificarSenhasAntigos()")
    class VerificarSenhasAntigos {

        @Test
        @DisplayName("deve enviar notificação para usuários com senha com mais de 90 dias")
        void deveNotificarSenhaAntiga() {
            Usuario usuario = usuarioComSenhaAntiga(91);
            usuario.setNotificacaoSenhaEnviada(false);

            when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

            service.verificarSenhasAntigos();

            verify(notificacaoRepository).save(any(NotificacaoUsuario.class));
            verify(usuarioRepository, atLeastOnce()).save(usuario);
            assertThat(usuario.isNotificacaoSenhaEnviada()).isTrue();
        }

        @Test
        @DisplayName("não deve notificar usuários com senha recente (menos de 90 dias)")
        void naoDeveNotificarSenhaRecente() {
            Usuario usuario = usuarioComSenhaAntiga(10);
            usuario.setNotificacaoSenhaEnviada(false);

            when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

            service.verificarSenhasAntigos();

            verify(notificacaoRepository, never()).save(any());
        }

        @Test
        @DisplayName("não deve reenviar notificação se já foi enviada anteriormente")
        void naoDeveReenviarNotificacao() {
            Usuario usuario = usuarioComSenhaAntiga(100);
            usuario.setNotificacaoSenhaEnviada(true);

            when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

            service.verificarSenhasAntigos();

            verify(notificacaoRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve processar múltiplos usuários corretamente")
        void deveProcessarMultiplosUsuarios() {
            Usuario com91Dias = usuarioComSenhaAntiga(91);
            com91Dias.setNotificacaoSenhaEnviada(false);

            Usuario com10Dias = usuarioComSenhaAntiga(10);
            com10Dias.setNotificacaoSenhaEnviada(false);

            when(usuarioRepository.findAll()).thenReturn(List.of(com91Dias, com10Dias));

            service.verificarSenhasAntigos();

            verify(notificacaoRepository, times(1)).save(any(NotificacaoUsuario.class));
        }
    }

    private Usuario usuarioComSenhaAntiga(int diasAtras) {
        return Usuario.builder()
                .username("joao").nome("joao").nomeCompleto("João Silva")
                .email("j@e.com").senha("h").roles(Set.of("USER")).enabled(true)
                .createdAt(Instant.now())
                .ultimaAlteracaoSenha(Instant.now().minus(diasAtras, ChronoUnit.DAYS))
                .notificacaoSenhaEnviada(false)
                .build();
    }
}
