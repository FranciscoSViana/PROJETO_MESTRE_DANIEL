package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.domain.model.NotificacaoUsuario;
import io.github.franciscosviana.stmservicos.domain.model.Usuario;
import io.github.franciscosviana.stmservicos.domain.repository.NotificacaoUsuarioRepository;
import io.github.franciscosviana.stmservicos.domain.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacaoSenhaService {

    private static final long DIAS_PARA_AVISO = 90;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private final UsuarioRepository usuarioRepository;
    private final NotificacaoUsuarioRepository notificacaoRepository;
    private final EmailService emailService;

    @Scheduled(fixedRate = 3_600_000)
    @Transactional
    public void verificarSenhasAntigos() {
        Instant limite = Instant.now().minus(DIAS_PARA_AVISO, ChronoUnit.DAYS);
        List<Usuario> usuarios = usuarioRepository.findAll();

        for (Usuario usuario : usuarios) {
            if (!usuario.isEnabled()) continue;
            Instant ultimaTroca = usuario.getUltimaAlteracaoSenha();
            if (ultimaTroca == null || ultimaTroca.isAfter(limite)) continue;
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
        String primeiroNome = primeiroNome(usuario.getNomeCompleto());

        String corpo = """
                <p>Olá, <strong>%s</strong>!</p>

                <table cellpadding="0" cellspacing="0" style="background:#fff7ed;border:1px solid #fed7aa;
                       border-radius:8px;padding:16px 20px;margin:20px 0;width:100%%;">
                  <tr>
                    <td>
                      <p style="margin:0;font-size:15px;color:#9a3412;">
                        ⚠️ <strong>Atenção:</strong> sua senha não é alterada há mais de 90 dias.
                      </p>
                    </td>
                  </tr>
                </table>

                <p>Por segurança, recomendamos que você atualize sua senha regularmente.
                   Caso não troque nos próximos dias, sua conta poderá ser bloqueada.</p>

                <p style="text-align:center;margin:28px 0 8px;">
                  <a href="%s"
                     style="display:inline-block;background:#1d4ed8;color:#ffffff;text-decoration:none;
                            padding:12px 32px;border-radius:6px;font-size:15px;font-weight:700;">
                    Acessar o sistema e trocar senha →
                  </a>
                </p>

                <p style="margin-top:28px;font-size:13px;color:#6b7280;">
                  Se já trocou sua senha recentemente, pode ignorar este aviso.
                </p>
                """.formatted(primeiroNome, frontendUrl);

        try {
            emailService.enviarEmail(
                    usuario.getEmail(),
                    "⚠️ Recomendação de troca de senha — GUARDIAN",
                    EmailService.template("Recomendação de segurança", corpo)
            );
        } catch (Exception e) {
            log.error("❌ Erro ao enviar e-mail de aviso de senha para {}", usuario.getEmail(), e);
        }
    }

    @Transactional
    public void registrarTrocaSenha(Usuario usuario) {
        usuario.setUltimaAlteracaoSenha(Instant.now());
        usuario.setNotificacaoSenhaEnviada(false);
        usuarioRepository.save(usuario);
    }

    private String primeiroNome(String nomeCompleto) {
        if (nomeCompleto == null) return "usuário";
        return nomeCompleto.contains(" ")
                ? nomeCompleto.substring(0, nomeCompleto.indexOf(' '))
                : nomeCompleto;
    }
}