package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.model.input.AuthRequest;
import io.github.franciscosviana.stmservicos.api.model.input.RegisterRequest;
import io.github.franciscosviana.stmservicos.api.model.output.AuthResponse;
import io.github.franciscosviana.stmservicos.common.validation.UsuarioException;
import io.github.franciscosviana.stmservicos.domain.model.Usuario;
import io.github.franciscosviana.stmservicos.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final AuthenticationManager authenticationManager;
    private final UsuarioDetailsService usuarioDetailsService;

    public AuthResponse login(AuthRequest request) {
        try {
            var authToken = new UsernamePasswordAuthenticationToken(
                    request.getUsuario(),
                    request.getSenha()
            );

            authenticationManager.authenticate(authToken);

            UserDetails userDetails =
                    usuarioDetailsService.loadUserByUsername(request.getUsuario());

            Map<String, Object> claims = Map.of(
                    "roles", userDetails.getAuthorities()
                            .stream()
                            .map(a -> a.getAuthority()) // ex: ROLE_ADMIN
                            .collect(Collectors.toList())
            );

            String token = jwtService.generateToken(userDetails.getUsername(), claims);

            return new AuthResponse(token, "Bearer");

        } catch (BadCredentialsException e) {
            throw new RuntimeException("Credenciais inválidas");
        }
    }

    public void cadastrar(RegisterRequest req) {

        if (usuarioRepository.existsByEmail(req.getEmail())) {
            throw new UsuarioException("E-mail já existe");
        }

        // ✅ REGRA DE NEGÓCIO DAS ROLES
        Set<String> roles = req.getRoles();

        if (roles == null || roles.isEmpty()) {
            roles = Set.of("USER");
        } else {
            for (String role : roles) {
                if (role == null || (!"USER".equals(role) && !"ADMIN".equals(role))) {
                    throw new RuntimeException("Role inválida: " + role);
                }
            }
        }

        Usuario usuario = Usuario.builder()
                .nome(req.getUsuario())
                .email(req.getEmail())
                .senha(passwordEncoder.encode(req.getSenha()))
                .roles(roles)
                .createdAt(Instant.now())
                .enabled(true)
                .build();

        usuarioRepository.save(usuario);

        emailService.enviarEmail(usuario.getEmail(), "Bem vindo ao sistema", "Olá " + usuario.getNome() + ", sua conta foi criada com sucesso!");
    }
}
