package io.github.franciscosviana.stmservicos.api.controller;

import io.github.franciscosviana.stmservicos.api.model.input.AuthRequest;
import io.github.franciscosviana.stmservicos.api.model.input.RegisterRequest;
import io.github.franciscosviana.stmservicos.api.model.input.ResetSenhaRequest;
import io.github.franciscosviana.stmservicos.api.model.input.UpdateUsuarioRequest;
import io.github.franciscosviana.stmservicos.api.model.output.AuthResponse;
import io.github.franciscosviana.stmservicos.api.model.output.UsuarioResponse;
import io.github.franciscosviana.stmservicos.common.validation.RoleException;
import io.github.franciscosviana.stmservicos.common.validation.SenhaFracaException;
import io.github.franciscosviana.stmservicos.common.validation.SenhaRepetidaException;
import io.github.franciscosviana.stmservicos.common.validation.UsuarioException;
import io.github.franciscosviana.stmservicos.domain.model.NotificacaoUsuario;
import io.github.franciscosviana.stmservicos.domain.model.Usuario;
import io.github.franciscosviana.stmservicos.domain.repository.NotificacaoUsuarioRepository;
import io.github.franciscosviana.stmservicos.domain.repository.UsuarioRepository;
import io.github.franciscosviana.stmservicos.domain.service.AuthService;
import io.github.franciscosviana.stmservicos.domain.service.RefreshTokenService;
import io.github.franciscosviana.stmservicos.domain.service.SenhaResetService;
import io.github.franciscosviana.stmservicos.domain.service.TokenService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenService tokenService;
    private final SenhaResetService senhaResetService;
    private final RefreshTokenService refreshTokenService;
    private final UsuarioRepository usuarioRepository;
    private final NotificacaoUsuarioRepository notificacaoRepository;

    // ─── LOGIN ───────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        log.info("✅ [AuthController] Login: {}", request.getUsuario());
        try {
            return ResponseEntity.ok(authService.login(request));
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── CADASTRO ────────────────────────────────
    @PostMapping("/cadastro")
    public ResponseEntity<?> cadastrar(@RequestBody @Valid RegisterRequest req) {
        try {
            authService.cadastrar(req);
            return ResponseEntity.ok(Map.of("message", "Cadastro realizado com sucesso"));
        } catch (UsuarioException | SenhaFracaException | RoleException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── REFRESH TOKEN ───────────────────────────
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        var token = refreshTokenService.validate(refreshToken);
        Usuario usuario = token.getUsuario();
        String newAccessToken = tokenService.generateToken(usuario.getUsername(), usuario.getRoles());
        return ResponseEntity.ok(new AuthResponse(newAccessToken, refreshToken, "Bearer"));
    }

    // ─── LISTAR USUÁRIOS ─────────────────────────
    @GetMapping("/usuarios")
    public Page<UsuarioResponse> listarUsuarios(@PageableDefault Pageable pageable) {
        return authService.listarUsuarios(pageable);
    }

    // ─── ATUALIZAR USUÁRIO ───────────────────────
    @PutMapping("/usuarios/{id}")
    public ResponseEntity<?> atualizarUsuario(@PathVariable UUID id,
                                              @RequestBody @Valid UpdateUsuarioRequest request) {
        try {
            authService.atualizarUsuario(id, request);
            return ResponseEntity.ok(Map.of("message", "Usuário atualizado com sucesso"));
        } catch (UsuarioException | RoleException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── EXCLUIR USUÁRIO ─────────────────────────
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<?> excluirUsuario(@PathVariable UUID id) {
        try {
            authService.excluirUsuario(id);
            return ResponseEntity.ok(Map.of("message", "Usuário excluído com sucesso"));
        } catch (UsuarioException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── ESQUECI SENHA ───────────────────────────
    @PostMapping("/esqueci-senha")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        senhaResetService.createPasswordResetTokenForEmail(body.get("email"));
        return ResponseEntity.ok(Map.of("message", "Se o e-mail existir, você receberá instruções"));
    }

    // ─── RESET SENHA ─────────────────────────────
    @PostMapping("/reset-senha")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetSenhaRequest req) {
        try {
            boolean ok = senhaResetService.resetPassword(req.getToken(), req.getNovaSenha());
            if (ok) return ResponseEntity.ok(Map.of("message", "Senha atualizada"));
            return ResponseEntity.badRequest().body(Map.of("error", "Token inválido ou expirado"));
        } catch (SenhaRepetidaException | SenhaFracaException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ─── NOTIFICAÇÕES DO USUÁRIO LOGADO ──────────

    /** Retorna notificações não lidas do usuário autenticado */
    @GetMapping("/notificacoes")
    public ResponseEntity<?> minhasNotificacoes(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getSubject();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow();
        List<NotificacaoUsuario> lista =
                notificacaoRepository.findByUsuarioAndLidaFalseOrderByCriadaEmDesc(usuario);
        return ResponseEntity.ok(lista);
    }

    /** Marca todas as notificações do usuário logado como lidas */
    @PostMapping("/notificacoes/marcar-lidas")
    @Transactional
    public ResponseEntity<?> marcarNotificacoesComoLidas(@AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getSubject();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow();
        notificacaoRepository.marcarTodasComoLidas(usuario);
        return ResponseEntity.ok(Map.of("message", "Notificações marcadas como lidas"));
    }
}