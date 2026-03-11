package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.model.input.AuthRequest;
import io.github.franciscosviana.stmservicos.api.model.input.RegisterRequest;
import io.github.franciscosviana.stmservicos.api.model.output.AuthResponse;
import io.github.franciscosviana.stmservicos.common.validation.RoleException;
import io.github.franciscosviana.stmservicos.common.validation.SenhaRepetidaException;
import io.github.franciscosviana.stmservicos.common.validation.UsuarioException;
import io.github.franciscosviana.stmservicos.domain.model.HistoricoSenha;
import io.github.franciscosviana.stmservicos.domain.model.Usuario;
import io.github.franciscosviana.stmservicos.domain.repository.HistoricoSenhaRepository;
import io.github.franciscosviana.stmservicos.domain.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final EmailService emailService;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final HistoricoSenhaRepository historicoSenhaRepository;

    public AuthResponse login(AuthRequest request) {

        var authToken = new UsernamePasswordAuthenticationToken(
                request.getUsuario(),
                request.getSenha()
        );

        authenticationManager.authenticate(authToken);

        Usuario user = usuarioRepository.findByNome(request.getUsuario())
                .orElseThrow();

        String accessToken = tokenService.generateToken(
                user.getNome(),
                user.getRoles()
        );

        var refreshToken = refreshTokenService.create(user);

        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                "Bearer"
        );
    }

    public Page<Usuario> listarUsuarios(Pageable pageable) {
        return usuarioRepository.findAll(pageable);
    }

    public void atualizarUsuario(UUID usuarioId, String novoNome, String novoEmail, Set<String> novasRoles) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioException("Usuário não encontrado"));

        // Validar email (não permitir duplicidade)
        if (!usuario.getEmail().equals(novoEmail) && usuarioRepository.existsByEmail(novoEmail)) {
            throw new UsuarioException("E-mail já existe");
        }

        // Validar roles
        if (novasRoles != null && !novasRoles.isEmpty()) {
            for (String role : novasRoles) {
                if (!"USER".equals(role) && !"ADMIN".equals(role)) {
                    throw new RoleException("Role inválida: " + role);
                }
            }
            usuario.setRoles(novasRoles);
        }

        // Atualizar campos
        usuario.setNome(novoNome != null ? novoNome : usuario.getNome());
        usuario.setEmail(novoEmail != null ? novoEmail : usuario.getEmail());

        usuarioRepository.save(usuario);
        log.info("✅ Usuário {} atualizado com sucesso", usuario.getNome());
    }


    @Transactional
    public void excluirUsuario(UUID usuarioId) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        if (usuarioOpt.isEmpty()) {
            throw new UsuarioException("Usuário não encontrado");
        }

        Usuario usuario = usuarioOpt.get();

        // Remove histórico de senhas antes de deletar o usuário
        historicoSenhaRepository.deleteAllByUsuarioId(usuario);

        // Remove usuário
        usuarioRepository.delete(usuario);
        log.info("✅ Usuário {} excluído com sucesso", usuario.getNome());
    }

    public void cadastrar(RegisterRequest req) {

        if (usuarioRepository.existsByEmail(req.getEmail())) {
            throw new UsuarioException("E-mail já existe");
        }

        Set<String> roles = req.getRoles();

        if (roles == null || roles.isEmpty()) {
            roles = Set.of("USER");
        } else {
            for (String role : roles) {
                if (role == null || (!"USER".equals(role) && !"ADMIN".equals(role))) {
                    throw new RoleException("Role inválida: " + role);
                }
            }
        }

        Usuario usuario = Usuario.builder()
                .nome(req.getNome())
                .email(req.getEmail())
                .senha(passwordEncoder.encode(req.getSenha()))
                .roles(roles)
                .createdAt(Instant.now())
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
            throw new UsuarioException("E-mail já existe");
        }

        try {
            emailService.enviarEmail(usuario.getEmail(), "Bem vindo ao sistema", "Olá " + usuario.getNome() + ", sua conta foi criada com sucesso!");
            log.info("📧 Email enviado para {}", usuario.getEmail());
        } catch (Exception e) {
            log.error("❌ Erro ao enviar email", e);
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

}
