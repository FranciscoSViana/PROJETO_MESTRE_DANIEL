package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.domain.model.HistoricoSenha;
import io.github.franciscosviana.stmservicos.domain.model.SenhaResetToken;
import io.github.franciscosviana.stmservicos.domain.model.Usuario;
import io.github.franciscosviana.stmservicos.domain.repository.HistoricoSenhaRepository;
import io.github.franciscosviana.stmservicos.domain.repository.SenhaResetTokenRepository;
import io.github.franciscosviana.stmservicos.domain.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class SenhaResetService {

    @Value("${app.frontend.reset-url}")
    private String frontendResetUrl;

    private final AuthService authService;
    private final EmailService emailService;
    private final BCryptPasswordEncoder encoder;
    private final UsuarioRepository usuarioRepo;
    private final SenhaResetTokenRepository tokenRepo;
    private final HistoricoSenhaRepository historicoSenhaRepo;
    private final NotificacaoSenhaService notificacaoSenhaService;

    @Transactional
    public void createPasswordResetTokenForEmail(String email) {
        var userOpt = usuarioRepo.findByEmail(email);
        if (userOpt.isEmpty()) return;

        Usuario user = userOpt.get();
        tokenRepo.deleteByUsuario(user);

        String token = java.util.UUID.randomUUID().toString();
        SenhaResetToken prt = SenhaResetToken.builder()
                .token(token)
                .usuario(user)
                .expiryDate(Instant.now().plusSeconds(3600))
                .build();
        tokenRepo.save(prt);

        String link = frontendResetUrl + token;
        String primeiroNome = primeiroNome(user.getNomeCompleto());

        String corpo = """
                <p>Olá, <strong>%s</strong>!</p>
                <p>Recebemos uma solicitação de recuperação de senha para sua conta.</p>
                <p>Clique no botão abaixo para definir uma nova senha.
                   Este link é válido por <strong>1 hora</strong>.</p>

                <p style="text-align:center;margin:28px 0 8px;">
                  <a href="%s"
                     style="display:inline-block;background:#1d4ed8;color:#ffffff;text-decoration:none;
                            padding:12px 32px;border-radius:6px;font-size:15px;font-weight:700;">
                    Redefinir senha →
                  </a>
                </p>
                <p style="text-align:center;font-size:12px;color:#9ca3af;">
                  Ou copie o link: <a href="%s" style="color:#1d4ed8;">%s</a>
                </p>

                <p style="margin-top:28px;font-size:13px;color:#6b7280;">
                  Se você não solicitou a recuperação de senha, ignore este e-mail.
                  Sua senha permanecerá a mesma.
                </p>
                """.formatted(primeiroNome, link, link, link);

        try {
            emailService.enviarEmail(
                    user.getEmail(),
                    "Recuperação de senha — GUARDIAN",
                    EmailService.template("Recuperação de senha", corpo)
            );
            log.info("📧 Link de recuperação enviado para {}", user.getEmail());
        } catch (Exception e) {
            log.error("❌ Erro ao enviar e-mail de recuperação", e);
        }
    }

    public boolean resetPassword(String token, String novaSenha) {
        authService.validarSenhaForte(novaSenha);

        var tokenOpt = tokenRepo.findByToken(token);
        if (tokenOpt.isEmpty()) return false;

        var prt = tokenOpt.get();
        if (prt.getExpiryDate().isBefore(Instant.now())) {
            tokenRepo.delete(prt);
            return false;
        }

        Usuario user = prt.getUsuario();
        authService.validarUltimasSenhas(user, novaSenha);

        user.setSenha(encoder.encode(novaSenha));
        usuarioRepo.save(user);

        historicoSenhaRepo.save(
                HistoricoSenha.builder()
                        .usuarioId(user)
                        .senhaHash(user.getSenha())
                        .criadaEm(Instant.now())
                        .build()
        );

        notificacaoSenhaService.registrarTrocaSenha(user);
        tokenRepo.delete(prt);
        return true;
    }

    @Scheduled(fixedRate = 1_200_000)
    @org.springframework.transaction.annotation.Transactional
    public void limparTokensExpirados() {
        tokenRepo.deleteByExpiryDateBefore(Instant.now());
        log.info("🧹 Tokens de reset expirados removidos");
    }

    private String primeiroNome(String nomeCompleto) {
        if (nomeCompleto == null) return "usuário";
        return nomeCompleto.contains(" ")
                ? nomeCompleto.substring(0, nomeCompleto.indexOf(' '))
                : nomeCompleto;
    }
}