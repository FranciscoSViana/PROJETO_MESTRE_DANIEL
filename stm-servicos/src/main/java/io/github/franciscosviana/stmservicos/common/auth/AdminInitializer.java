package io.github.franciscosviana.stmservicos.common.auth;

import io.github.franciscosviana.stmservicos.domain.model.Usuario;
import io.github.franciscosviana.stmservicos.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;

    @Override
    public void run(String... args) throws Exception {
        if (!usuarioRepository.existsByNome("admin")) {
            Usuario admin = Usuario.builder()
                    .nomeCompleto("Admin")
                    .username("admin")          // ← adicionar
                    .nome("admin")
                    .email("admin@sistema.com")
                    .senha(passwordEncoder.encode("admin"))
                    .roles(Set.of("ADMIN"))
                    .createdAt(Instant.now())
                    .enabled(true)
                    .build();

            usuarioRepository.save(admin);

            System.out.println("✅ Usuário ADMIN criado com sucesso.");
        }
    }
}
