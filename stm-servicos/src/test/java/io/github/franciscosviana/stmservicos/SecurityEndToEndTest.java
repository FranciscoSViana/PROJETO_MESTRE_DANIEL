package io.github.franciscosviana.stmservicos;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes end-to-end que sobem o contexto completo do Spring Boot com H2.
 * Validam as rotas públicas e o comportamento de segurança da aplicação.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("End-to-End: Segurança e rotas públicas")
class SecurityEndToEndTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    // ──────────────────────────────────────────────────────────────────────────
    // Rotas públicas (não precisam de auth)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/login com body vazio deve retornar 400 (não 401/403)")
    void loginSemBodyDeveRetornarBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/cadastro com body vazio deve retornar 4xx (validação)")
    void cadastroSemBodyDeveRetornarErro() throws Exception {
        mockMvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("GET /api/auth/username-preview deve ser acessível sem autenticação")
    void usernamePreviewPublico() throws Exception {
        mockMvc.perform(get("/api/auth/username-preview")
                        .param("nome", "Jo"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/auth/esqueci-senha deve ser acessível sem autenticação")
    void esqueciSenhaPublico() throws Exception {
        mockMvc.perform(post("/api/auth/esqueci-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nao-existe@test.com\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/rastreio/status deve ser acessível sem autenticação via Swagger")
    void statusRastreioNaoExigeAuth() throws Exception {
        // Esta rota não está explicitamente em permitAll, mas devemos validar comportamento
        mockMvc.perform(get("/api/rastreio/status"))
                .andExpect(status().is2xxSuccessful());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Rotas protegidas (exigem token)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/clientes sem token deve retornar 401")
    void clientesSemTokenDeveRetornar401() throws Exception {
        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/ordens-servico sem token deve retornar 401")
    void ordensSemTokenDeveRetornar401() throws Exception {
        mockMvc.perform(get("/api/ordens-servico"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/credenciados sem token deve retornar 401")
    void credenciadosSemTokenDeveRetornar401() throws Exception {
        mockMvc.perform(get("/api/credenciados"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/financeiro/contas-pagar sem token deve retornar 401")
    void contasPagarSemTokenDeveRetornar401() throws Exception {
        mockMvc.perform(get("/api/financeiro/contas-pagar"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/solucoes sem token deve retornar 401")
    void solucoesSemTokenDeveRetornar401() throws Exception {
        mockMvc.perform(get("/api/solucoes"))
                .andExpect(status().isUnauthorized());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Swagger / OpenAPI (rotas públicas de documentação)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /v3/api-docs deve estar acessível sem autenticação")
    void swaggerApiDocsPublico() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }
}
