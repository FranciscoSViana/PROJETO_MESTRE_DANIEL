package io.github.franciscosviana.stmservicos.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TokenService")
class TokenServiceTest {

    private TokenService tokenService;

    // Chave de 32+ chars para HmacSHA256
    private static final String SECRET = "test-secret-key-for-tests-only-32chars!!";

    @BeforeEach
    void setUp() {
        tokenService = new TokenService(SECRET);
    }

    @Test
    @DisplayName("deve gerar token JWT não nulo e não vazio")
    void deveGerarToken() {
        String token = tokenService.generateToken("joao.silva", Set.of("USER"));

        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("token gerado deve ter 3 partes separadas por ponto (header.payload.signature)")
    void deveGerarTokenComTresPartes() {
        String token = tokenService.generateToken("joao.silva", Set.of("USER"));

        String[] partes = token.split("\\.");
        assertThat(partes).hasSize(3);
    }

    @Test
    @DisplayName("payload do token deve conter o subject (username)")
    void payloadDeveConterUsername() {
        String token = tokenService.generateToken("joao.silva", Set.of("USER"));

        String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
        assertThat(payload).contains("joao.silva");
    }

    @Test
    @DisplayName("payload do token deve conter as roles")
    void payloadDeveConterRoles() {
        String token = tokenService.generateToken("admin.user", Set.of("ADMIN"));

        String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
        assertThat(payload).contains("ADMIN");
    }

    @Test
    @DisplayName("deve gerar tokens diferentes para usuários distintos")
    void deveGerarTokensDiferentesParaUsuariosDiferentes() {
        String token1 = tokenService.generateToken("usuario1", Set.of("USER"));
        String token2 = tokenService.generateToken("usuario2", Set.of("USER"));

        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    @DisplayName("payload deve conter issuer 'stm-api'")
    void payloadDeveConterIssuer() {
        String token = tokenService.generateToken("joao.silva", Set.of("USER"));

        String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
        assertThat(payload).contains("stm-api");
    }
}
