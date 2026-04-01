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
        if (userOpt.isEmpty()) return; // não vazar se e-mail existe

        Usuario user = userOpt.get();
        tokenRepo.deleteByUsuario(user);

        String token = java.util.UUID.randomUUID().toString();

        SenhaResetToken prt = SenhaResetToken.builder()
                .token(token)
                .usuario(user)
                .expiryDate(Instant.now().plusSeconds(3600)) // 1h
                .build();

        tokenRepo.save(prt);

        String link = frontendResetUrl + token;

        try {
            emailService.enviarEmail(
                    user.getEmail(),
                    "Recuperação de senha — STM Serviços",
                    "Olá " + user.getNomeCompleto() + ",\n\n" +
                            "Recebemos uma solicitação de recuperação de senha.\n" +
                            "Clique no link abaixo para definir uma nova senha (válido por 1 hora):\n\n" +
                            link + "\n\n" +
                            "Se não foi você, ignore este e-mail.\n\n" +
                            "Atenciosamente,\nEquipe STM Serviços"
            );
            log.info("📧 Link de recuperação enviado para {}", user.getEmail());
        } catch (Exception e) {
            log.error("❌ Erro ao enviar e-mail de recuperação", e);
        }
    }

    public boolean resetPassword(String token, String novaSenha) {

        // Valida força da senha antes de qualquer coisa
        authService.validarSenhaForte(novaSenha);

        var tokenOpt = tokenRepo.findByToken(token);
        if (tokenOpt.isEmpty()) return false;

        var prt = tokenOpt.get();

        if (prt.getExpiryDate().isBefore(Instant.now())) {
            tokenRepo.delete(prt);
            return false;
        }

        Usuario user = prt.getUsuario();

        // Valida histórico de senhas
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

        // Reseta ciclo de notificação de senha antiga
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
}