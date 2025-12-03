package io.github.franciscosviana.stmservicos.api.controller;

import io.github.franciscosviana.stmservicos.api.model.input.AuthRequest;
import io.github.franciscosviana.stmservicos.api.model.input.RegisterRequest;
import io.github.franciscosviana.stmservicos.domain.service.AuthService;
import io.github.franciscosviana.stmservicos.domain.service.SenhaResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final SenhaResetService senhaResetService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest req) {
        authService.register(req);
        return ResponseEntity.ok(Map.of("message", "registrado com sucesso"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        senhaResetService.createPasswordResetTokenForEmail(email);
        return ResponseEntity.ok(Map.of("message", "Se o e-mail existe, você receberá instruções"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");

        boolean ok = senhaResetService.resetPassword(token, newPassword);

        if (ok)
            return ResponseEntity.ok(Map.of("message", "Senha atualizada"));

        return ResponseEntity.badRequest()
                .body(Map.of("error", "Token inválido ou expirado"));
    }
}
