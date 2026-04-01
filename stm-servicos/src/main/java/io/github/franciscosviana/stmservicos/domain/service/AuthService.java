package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.model.input.AuthRequest;
import io.github.franciscosviana.stmservicos.api.model.input.RegisterRequest;
import io.github.franciscosviana.stmservicos.api.model.input.UpdateUsuarioRequest;
import io.github.franciscosviana.stmservicos.api.model.output.AuthResponse;
import io.github.franciscosviana.stmservicos.api.model.output.UsuarioResponse;
import io.github.franciscosviana.stmservicos.common.validation.RoleException;
import io.github.franciscosviana.stmservicos.common.validation.SenhaFracaException;
import io.github.franciscosviana.stmservicos.common.validation.SenhaRepetidaException;
import io.github.franciscosviana.stmservicos.common.validation.UsuarioException;
import io.github.franciscosviana.stmservicos.domain.model.HistoricoSenha;
import io.github.franciscosviana.stmservicos.domain.model.Usuario;
import io.github.franciscosviana.stmservicos.domain.repository.HistoricoSenhaRepository;
import io.github.franciscosviana.stmservicos.domain.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Pattern SENHA_FORTE = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$"
    );

    /** URL base do frontend — ex: https://meuapp.com/ */
    @Value("${app.frontend.url}")
    private String frontendUrl;

    private final EmailService emailService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final HistoricoSenhaRepository historicoSenhaRepository;
    private final UsernameGeneratorService usernameGeneratorService;

    // ─────────────────────────────────────────────
    //  LOGIN
    // ─────────────────────────────────────────────
    public AuthResponse login(AuthRequest request) {
        var authToken = new UsernamePasswordAuthenticationToken(
                request.getUsuario(), request.getSenha());
        authenticationManager.authenticate(authToken);

        Usuario user = usuarioRepository.findByUsername(request.getUsuario())
                .or(() -> usuarioRepository.findByEmail(request.getUsuario()))
                .orElseThrow(() -> new UsuarioException("Usuário não encontrado"));

        String accessToken = tokenService.generateToken(user.getUsername(), user.getRoles());
        var refreshToken = refreshTokenService.create(user);
        return new AuthResponse(accessToken, refreshToken.getToken(), "Bearer");
    }

    // ─────────────────────────────────────────────
    //  CADASTRO
    // ─────────────────────────────────────────────
    @Transactional
    public void cadastrar(RegisterRequest req) {
        validarSenhaForte(req.getSenha());

        if (usuarioRepository.existsByEmail(req.getEmail())) {
            throw new UsuarioException("E-mail já cadastrado");
        }

        Set<String> roles = req.getRoles();
        if (roles == null || roles.isEmpty()) {
            roles = Set.of("USER");
        } else {
            validarRoles(roles);
        }

        String username = usernameGeneratorService.gerar(req.getNomeCompleto());

        Usuario usuario = Usuario.builder()
                .nomeCompleto(req.getNomeCompleto())
                .username(username)
                .nome(username)
                .email(req.getEmail())
                .senha(passwordEncoder.encode(req.getSenha()))
                .dataNascimento(req.getDataNascimento())
                .roles(roles)
                .createdAt(Instant.now())
                .ultimaAlteracaoSenha(Instant.now())
                .enabled(true)
                .build();

        try {
            usuarioRepository.save(usuario);
            historicoSenhaRepository.save(
                    HistoricoSenha.builder()
                            .usuarioId(usuario)
                            .senhaHash(usuario.getSenha())
                            .criadaEm(Instant.now())
                            .build()
            );
        } catch (DataIntegrityViolationException e) {
            throw new UsuarioException("E-mail já cadastrado");
        }

        try {
            emailService.enviarEmail(
                    usuario.getEmail(),
                    "Bem-vindo a GUARDIAN",
                    emailBoasVindas(usuario.getNomeCompleto(), username)
            );
        } catch (Exception e) {
            log.error("❌ Erro ao enviar e-mail de boas-vindas", e);
        }

        log.info("✅ Usuário {} cadastrado com username '{}'", usuario.getNomeCompleto(), username);
    }

    // ─────────────────────────────────────────────
    //  LISTAGEM
    // ─────────────────────────────────────────────
    public Page<UsuarioResponse> listarUsuarios(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(this::toResponse);
    }

    // ─────────────────────────────────────────────
    //  ATUALIZAÇÃO
    // ─────────────────────────────────────────────
    @Transactional
    public void atualizarUsuario(UUID usuarioId, UpdateUsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioException("Usuário não encontrado"));

        if (request.getEmail() != null
                && !request.getEmail().equals(usuario.getEmail())
                && usuarioRepository.existsByEmail(request.getEmail())) {
            throw new UsuarioException("E-mail já cadastrado");
        }

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            validarRoles(request.getRoles());
            usuario.setRoles(request.getRoles());
        }
        if (request.getNomeCompleto() != null) usuario.setNomeCompleto(request.getNomeCompleto());
        if (request.getEmail() != null)        usuario.setEmail(request.getEmail());
        if (request.getDataNascimento() != null) usuario.setDataNascimento(request.getDataNascimento());

        usuarioRepository.save(usuario);
        log.info("✅ Usuário {} atualizado", usuario.getUsername());
    }

    // ─────────────────────────────────────────────
    //  EXCLUSÃO
    // ─────────────────────────────────────────────
    @Transactional
    public void excluirUsuario(UUID usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioException("Usuário não encontrado"));

        historicoSenhaRepository.deleteAllByUsuarioId(usuario);
        usuarioRepository.delete(usuario);
        log.info("✅ Usuário {} excluído", usuario.getUsername());
    }

    // ─────────────────────────────────────────────
    //  VALIDAÇÕES
    // ─────────────────────────────────────────────
    public void validarSenhaForte(String senha) {
        if (senha == null || !SENHA_FORTE.matcher(senha).matches()) {
            throw new SenhaFracaException(
                    "A senha deve ter no mínimo 8 caracteres, contendo letra maiúscula, " +
                            "letra minúscula, número e caractere especial."
            );
        }
    }

    public void validarUltimasSenhas(Usuario usuario, String novaSenha) {
        List<HistoricoSenha> ultimas =
                historicoSenhaRepository.findTop5ByUsuarioIdOrderByCriadaEmDesc(usuario);
        for (HistoricoSenha h : ultimas) {
            if (passwordEncoder.matches(novaSenha, h.getSenhaHash())) {
                throw new SenhaRepetidaException(
                        "A nova senha não pode ser igual às últimas 5 senhas utilizadas."
                );
            }
        }
    }

    private void validarRoles(Set<String> roles) {
        for (String role : roles) {
            if (!"USER".equals(role) && !"ADMIN".equals(role)) {
                throw new RoleException("Role inválida: " + role);
            }
        }
    }

    // ─────────────────────────────────────────────
    //  MAPEAMENTO
    // ─────────────────────────────────────────────
    private UsuarioResponse toResponse(Usuario u) {
        UsuarioResponse dto = new UsuarioResponse();
        dto.setId(u.getId());
        dto.setNomeCompleto(u.getNomeCompleto());
        dto.setUsername(u.getUsername());
        dto.setEmail(u.getEmail());
        dto.setDataNascimento(u.getDataNascimento());
        dto.setIdade(u.getIdade());
        dto.setRoles(u.getRoles());
        dto.setEnabled(u.isEnabled());
        return dto;
    }

    // ─────────────────────────────────────────────
    //  TEMPLATES DE E-MAIL
    // ─────────────────────────────────────────────
    private String emailBoasVindas(String nomeCompleto, String username) {
        String loginUrl = frontendUrl;
        String primeiroNome = nomeCompleto != null && nomeCompleto.contains(" ")
                ? nomeCompleto.substring(0, nomeCompleto.indexOf(' '))
                : nomeCompleto;

        String corpo = """
                <p>Olá, <strong>%s</strong>!</p>
                <p>Sua conta na <strong>GUARDIAN</strong> foi criada com sucesso. 🎉</p>

                <table cellpadding="0" cellspacing="0" style="background:#f0f9ff;border:1px solid #bae6fd;
                       border-radius:8px;padding:16px 20px;margin:20px 0;width:100%%;">
                  <tr>
                    <td>
                      <p style="margin:0 0 6px;color:#0369a1;font-size:13px;font-weight:700;
                                 text-transform:uppercase;letter-spacing:.5px;">
                        Seus dados de acesso
                      </p>
                      <p style="margin:4px 0;font-size:15px;">
                        <strong>Usuário:</strong>
                        <span style="font-family:monospace;background:#e0f2fe;padding:2px 8px;
                                     border-radius:4px;color:#0c4a6e;">%s</span>
                      </p>
                      <p style="margin:4px 0;font-size:13px;color:#64748b;">
                        Use este usuário junto com a senha que você cadastrou para entrar no sistema.
                      </p>
                    </td>
                  </tr>
                </table>

                <p style="text-align:center;margin:28px 0 8px;">
                  <a href="%s"
                     style="display:inline-block;background:#1d4ed8;color:#ffffff;text-decoration:none;
                            padding:12px 32px;border-radius:6px;font-size:15px;font-weight:700;">
                    Acessar o sistema →
                  </a>
                </p>
                <p style="text-align:center;font-size:12px;color:#9ca3af;">
                  Ou copie o link: <a href="%s" style="color:#1d4ed8;">%s</a>
                </p>

                <p style="margin-top:28px;font-size:13px;color:#6b7280;">
                  Se você não solicitou este cadastro, ignore este e-mail.
                </p>
                """.formatted(primeiroNome, username, loginUrl, loginUrl, loginUrl);

        return EmailService.template("Bem-vindo ao sistema", corpo);
    }
}