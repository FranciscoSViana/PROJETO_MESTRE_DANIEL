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
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
    private final JavaMailSender mailSender;
    private final BCryptPasswordEncoder encoder;
    private final UsuarioRepository usuarioRepo;
    private final SenhaResetTokenRepository tokenRepo;
    private final HistoricoSenhaRepository historicoSenhaRepo;

    @Transactional
    public void createPasswordResetTokenForEmail(String email) {

        var userOpt = usuarioRepo.findByEmail(email);
        if (userOpt.isEmpty()) return; // para não vazar se email existe: sempre retornar 200

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

        var message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Recuperação de senha");
        message.setText("Para resetar sua senha, clique no link: " + link);

        mailSender.send(message);
    }

    public boolean resetPassword(String token, String novaSenha) {

        var tokenOpt = tokenRepo.findByToken(token);

        if (tokenOpt.isEmpty()) return false;

        var prt = tokenOpt.get();

        if (prt.getExpiryDate().isBefore(Instant.now())) {
            tokenRepo.delete(prt);
            return false;
        }

        Usuario user = prt.getUsuario();

        user.setSenha(encoder.encode(novaSenha));

        authService.validarUltimasSenhas(user, novaSenha);

        usuarioRepo.save(user);

        historicoSenhaRepo.save(
                HistoricoSenha.builder()
                        .usuarioId(user)
                        .senhaHash(user.getSenha())
                        .criadaEm(Instant.now())
                        .build()
        );

        tokenRepo.delete(prt);
        return true;
    }

    @Scheduled(fixedRate = 600000) // a cada 10 minutos
    public void limparTokensExpirados() {
        tokenRepo.deleteByExpiryDateBefore(Instant.now());
        log.info("🧹 Tokens de reset expirados removidos");
    }

}
