package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.domain.model.SenhaResetToken;
import io.github.franciscosviana.stmservicos.domain.model.Usuario;
import io.github.franciscosviana.stmservicos.domain.repository.SenhaResetTokenRepository;
import io.github.franciscosviana.stmservicos.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class SenhaResetService {

    private final SenhaResetTokenRepository tokenRepo;
    private final UsuarioRepository usuarioRepo;
    private final JavaMailSender mailSender;
    private final BCryptPasswordEncoder encoder;
    @Value("${app.frontend.reset-url}") // ex: https://app.me/reset-password?token=
    private String frontendResetUrl;

    public void createPasswordResetTokenForEmail(String email) {
        var userOpt = usuarioRepo.findByEmail(email);
        if (userOpt.isEmpty()) return; // para não vazar se email existe: sempre retornar 200

        Usuario user = userOpt.get();
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

    public boolean resetPassword(String token, String newPassword) {
        var tokenOpt = tokenRepo.findByToken(token);
        if (tokenOpt.isEmpty()) return false;

        var prt = tokenOpt.get();
        if (prt.getExpiryDate().isBefore(Instant.now())) {
            tokenRepo.delete(prt);
            return false;
        }
        Usuario user = prt.getUsuario();
        user.setSenha(encoder.encode(newPassword));
        usuarioRepo.save(user);
        tokenRepo.delete(prt);
        return true;
    }
}
