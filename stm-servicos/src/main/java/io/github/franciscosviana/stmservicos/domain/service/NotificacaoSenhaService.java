package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.domain.model.NotificacaoUsuario;
import io.github.franciscosviana.stmservicos.domain.model.Usuario;
import io.github.franciscosviana.stmservicos.domain.repository.NotificacaoUsuarioRepository;
import io.github.franciscosviana.stmservicos.domain.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * A cada hora verifica usuários cuja senha tem 90+ dias sem troca.
 * Para cada um: cria notificação em tela e envia e-mail (uma vez por ciclo).
 * O flag notificacaoSenhaEnviada é resetado quando o usuário troca a senha.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacaoSenhaService {

    private static final long DIAS_PARA_AVISO = 90;

    private final UsuarioRepository usuarioRepository;
    private final NotificacaoUsuarioRepository notificacaoRepository;
    private final EmailService emailService;

    // Roda a cada hora (3_600_000 ms)
    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void verificarSenhasAntigos() {

        Instant limite = Instant.now().minus(DIAS_PARA_AVISO, ChronoUnit.DAYS);

        List<Usuario> usuarios = usuarioRepository.findAll();

        for (Usuario usuario : usuarios) {
            if (!usuario.isEnabled()) continue;

            Instant ultimaTroca = usuario.getUltimaAlteracaoSenha();
            if (ultimaTroca == null || ultimaTroca.isAfter(limite)) continue;

            // Já foi notificado neste ciclo?
            if (usuario.isNotificacaoSenhaEnviada()) continue;

            criarNotificacaoTela(usuario);
            enviarEmailAviso(usuario);

            usuario.setNotificacaoSenhaEnviada(true);
            usuarioRepository.save(usuario);

            log.info("🔔 Aviso de troca de senha enviado para {}", usuario.getUsername());
        }
    }

    private void criarNotificacaoTela(Usuario usuario) {
        notificacaoRepository.save(
                NotificacaoUsuario.builder()
                        .usuario(usuario)
                        .tipo("TROCAR_SENHA")
                        .mensagem("⚠️ Sua senha não é alterada há mais de 90 dias. " +
                                "Por segurança, recomendamos que você a troque o quanto antes.")
                        .lida(false)
                        .criadaEm(Instant.now())
                        .build()
        );
    }

    private void enviarEmailAviso(Usuario usuario) {
        try {
            emailService.enviarEmail(
                    usuario.getEmail(),
                    "⚠️ Recomendação de troca de senha — STM Serviços",
                    "Olá " + usuario.getNomeCompleto() + ",\n\n" +
                            "Sua senha não é alterada há mais de 90 dias.\n" +
                            "Por segurança, recomendamos que você acesse o sistema e atualize sua senha.\n\n" +
                            "Caso não troque a senha, sua conta poderá ser bloqueada futuramente.\n\n" +
                            "Atenciosamente,\nEquipe STM Serviços"
            );
        } catch (Exception e) {
            log.error("❌ Erro ao enviar e-mail de aviso de senha para {}", usuario.getEmail(), e);
        }
    }

    /**
     * Chamado após o usuário trocar a senha com sucesso.
     * Reseta o ciclo de notificação.
     */
    @Transactional
    public void registrarTrocaSenha(Usuario usuario) {
        usuario.setUltimaAlteracaoSenha(Instant.now());
        usuario.setNotificacaoSenhaEnviada(false);
        usuarioRepository.save(usuario);
    }
}