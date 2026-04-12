package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.common.validation.SenhaFracaException;
import io.github.franciscosviana.stmservicos.domain.model.HistoricoSenha;
import io.github.franciscosviana.stmservicos.domain.model.SenhaResetToken;
import io.github.franciscosviana.stmservicos.domain.model.Usuario;
import io.github.franciscosviana.stmservicos.domain.repository.HistoricoSenhaRepository;
import io.github.franciscosviana.stmservicos.domain.repository.SenhaResetTokenRepository;
import io.github.franciscosviana.stmservicos.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SenhaResetService")
class SenhaResetServiceTest {

    @Mock
    private AuthService authService;
    @Mock
    private EmailService emailService;
    @Mock
    private BCryptPasswordEncoder encoder;
    @Mock
    private UsuarioRepository usuarioRepo;
    @Mock
    private SenhaResetTokenRepository tokenRepo;
    @Mock
    private HistoricoSenhaRepository historicoSenhaRepo;
    @Mock
    private NotificacaoSenhaService notificacaoSenhaService;

    @InjectMocks
    private SenhaResetService service;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "frontendResetUrl", "http://localhost:4200/reset?token=");

        usuario = Usuario.builder()
                .username("joao.silva")
                .nome("joao.silva")
                .nomeCompleto("João Silva")
                .email("joao@email.com")
                .senha("hash-atual")
                .roles(Set.of("USER"))
                .createdAt(Instant.now())
                .ultimaAlteracaoSenha(Instant.now())
                .build();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // createPasswordResetTokenForEmail()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("createPasswordResetTokenForEmail()")
    class CreateToken {

        @Test
        @DisplayName("deve criar token e enviar email quando usuário existe")
        void deveCriarTokenEEnviarEmail() {
            when(usuarioRepo.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));

            service.createPasswordResetTokenForEmail("joao@email.com");

            verify(tokenRepo).deleteByUsuario(usuario);
            ArgumentCaptor<SenhaResetToken> captor = ArgumentCaptor.forClass(SenhaResetToken.class);
            verify(tokenRepo).save(captor.capture());

            SenhaResetToken salvo = captor.getValue();
            assertThat(salvo.getToken()).isNotBlank();
            assertThat(salvo.getUsuario()).isEqualTo(usuario);
            assertThat(salvo.getExpiryDate()).isAfter(Instant.now());

            verify(emailService).enviarEmail(eq("joao@email.com"), any(), any());
        }

        @Test
        @DisplayName("deve não fazer nada quando email não existe (sem vazar informação)")
        void deveIgnorarEmailInexistente() {
            when(usuarioRepo.findByEmail("nao-existe@email.com")).thenReturn(Optional.empty());

            service.createPasswordResetTokenForEmail("nao-existe@email.com");

            verify(tokenRepo, never()).save(any());
            verify(emailService, never()).enviarEmail(any(), any(), any());
        }

        @Test
        @DisplayName("não deve lançar exceção se envio de email falhar")
        void naoDeveFalharSeEmailFalhar() {
            when(usuarioRepo.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
            doThrow(new RuntimeException("Serviço indisponível"))
                    .when(emailService).enviarEmail(any(), any(), any());

            // Não deve propagar a exceção
            service.createPasswordResetTokenForEmail("joao@email.com");

            verify(tokenRepo).save(any());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // resetPassword()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("resetPassword()")
    class ResetPassword {

        @Test
        @DisplayName("deve resetar senha com sucesso e retornar true")
        void deveResetarSenha() {
            SenhaResetToken prt = SenhaResetToken.builder()
                    .token("token-valido")
                    .usuario(usuario)
                    .expiryDate(Instant.now().plus(1, ChronoUnit.HOURS))
                    .build();

            when(tokenRepo.findByToken("token-valido")).thenReturn(Optional.of(prt));
            doNothing().when(authService).validarSenhaForte("NovaSenha@456");
            doNothing().when(authService).validarUltimasSenhas(usuario, "NovaSenha@456");
            when(encoder.encode("NovaSenha@456")).thenReturn("nova-hash");

            boolean resultado = service.resetPassword("token-valido", "NovaSenha@456");

            assertThat(resultado).isTrue();
            assertThat(usuario.getSenha()).isEqualTo("nova-hash");
            verify(usuarioRepo).save(usuario);
            verify(historicoSenhaRepo).save(any(HistoricoSenha.class));
            verify(notificacaoSenhaService).registrarTrocaSenha(usuario);
            verify(tokenRepo).delete(prt);
        }

        @Test
        @DisplayName("deve retornar false quando token não existe")
        void deveRetornarFalseSeTokenInexistente() {
            when(tokenRepo.findByToken("invalido")).thenReturn(Optional.empty());
            doNothing().when(authService).validarSenhaForte(any());

            assertThat(service.resetPassword("invalido", "NovaSenha@456")).isFalse();
        }

        @Test
        @DisplayName("deve retornar false e deletar token quando expirado")
        void deveRetornarFalseSeTokenExpirado() {
            SenhaResetToken prt = SenhaResetToken.builder()
                    .token("token-expirado")
                    .usuario(usuario)
                    .expiryDate(Instant.now().minus(1, ChronoUnit.HOURS))
                    .build();

            when(tokenRepo.findByToken("token-expirado")).thenReturn(Optional.of(prt));
            doNothing().when(authService).validarSenhaForte(any());

            boolean resultado = service.resetPassword("token-expirado", "NovaSenha@456");

            assertThat(resultado).isFalse();
            verify(tokenRepo).delete(prt);
            verify(usuarioRepo, never()).save(any());
        }

        @Test
        @DisplayName("deve propagar SenhaFracaException quando nova senha é fraca")
        void deveProprarExcecaoSeSenhaFraca() {
            doThrow(new SenhaFracaException("Senha fraca"))
                    .when(authService).validarSenhaForte("fraca");

            assertThatThrownBy(() -> service.resetPassword("qualquer-token", "fraca"))
                    .isInstanceOf(SenhaFracaException.class);
        }
    }
}
