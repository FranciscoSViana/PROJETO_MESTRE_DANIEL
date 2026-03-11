package io.github.franciscosviana.stmservicos.api.controller;

import io.github.franciscosviana.stmservicos.api.model.input.AuthRequest;
import io.github.franciscosviana.stmservicos.api.model.input.RegisterRequest;
import io.github.franciscosviana.stmservicos.api.model.input.ResetSenhaRequest;
import io.github.franciscosviana.stmservicos.api.model.input.UpdateUsuarioRequest;
import io.github.franciscosviana.stmservicos.api.model.output.AuthResponse;
import io.github.franciscosviana.stmservicos.common.validation.RoleException;
import io.github.franciscosviana.stmservicos.common.validation.SenhaRepetidaException;
import io.github.franciscosviana.stmservicos.common.validation.UsuarioException;
import io.github.franciscosviana.stmservicos.domain.model.Usuario;
import io.github.franciscosviana.stmservicos.domain.service.AuthService;
import io.github.franciscosviana.stmservicos.domain.service.RefreshTokenService;
import io.github.franciscosviana.stmservicos.domain.service.SenhaResetService;
import io.github.franciscosviana.stmservicos.domain.service.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {

        log.info("✅ [AuthController] Requisição de login recebida");
        log.info("➡️ Usuário recebido: " + request.getUsuario());

        try {
            var response = authService.login(request);
            log.info("✅ [AuthController] Login realizado com sucesso");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/cadastro")
    public ResponseEntity<?> cadastrar(@RequestBody @Valid RegisterRequest req) {
        try {
            authService.cadastrar(req);
            return ResponseEntity.ok(Map.of("message", "registrado com sucesso"));
        } catch (UsuarioException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> body) {

        String refreshToken = body.get("refreshToken");

        var token = refreshTokenService.validate(refreshToken);

        Usuario usuario = token.getUsuario();

        String newAccessToken = tokenService.generateToken(
                usuario.getNome(),
                usuario.getRoles()
        );

        return ResponseEntity.ok(
                new AuthResponse(newAccessToken, refreshToken, "Bearer")
        );
    }

    @GetMapping("/usuarios")
    public Page<Usuario> listarUsuarios(@PageableDefault Pageable pageable) {
        return authService.listarUsuarios(pageable);
    }

    @PutMapping("/usuarios/{id}")
    public ResponseEntity<?> atualizarUsuario(@PathVariable UUID id,
                                              @RequestBody UpdateUsuarioRequest request) {
        try {
            authService.atualizarUsuario(id, request.getNome(), request.getEmail(), request.getRoles());
            return ResponseEntity.ok(Map.of("message", "Usuário atualizado com sucesso"));
        } catch (UsuarioException | RoleException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<?> excluirUsuario(@PathVariable UUID id) {
        try {
            authService.excluirUsuario(id);
            return ResponseEntity.ok(Map.of("message", "Usuário excluído com sucesso"));
        } catch (UsuarioException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/esqueci-senha")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        senhaResetService.createPasswordResetTokenForEmail(email);
        return ResponseEntity.ok(Map.of("message", "Se o e-mail existe, você receberá instruções"));
    }

    @PostMapping("/reset-senha")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetSenhaRequest resetSenhaRequest) {

        try {
            boolean ok = senhaResetService.resetPassword(
                    resetSenhaRequest.getToken(),
                    resetSenhaRequest.getNovaSenha()
            );

            if (ok) {
                return ResponseEntity.ok(Map.of("message", "Senha atualizada"));
            }

            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Token inválido ou expirado"));

        } catch (SenhaRepetidaException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
