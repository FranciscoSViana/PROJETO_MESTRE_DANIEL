package io.github.franciscosviana.stmservicos.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.franciscosviana.stmservicos.api.exceptionhandler.ApiExceptionHandler;
import io.github.franciscosviana.stmservicos.api.model.input.AuthRequest;
import io.github.franciscosviana.stmservicos.api.model.input.RegisterRequest;
import io.github.franciscosviana.stmservicos.api.model.output.AuthResponse;
import io.github.franciscosviana.stmservicos.common.validation.CredenciaisInvalidasException;
import io.github.franciscosviana.stmservicos.common.validation.SenhaFracaException;
import io.github.franciscosviana.stmservicos.common.validation.UsuarioException;
import io.github.franciscosviana.stmservicos.config.TestSecurityConfig;
import io.github.franciscosviana.stmservicos.domain.model.RefreshToken;
import io.github.franciscosviana.stmservicos.domain.model.Usuario;
import io.github.franciscosviana.stmservicos.domain.repository.NotificacaoUsuarioRepository;
import io.github.franciscosviana.stmservicos.domain.repository.UsuarioRepository;
import io.github.franciscosviana.stmservicos.domain.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@DisplayName("AuthController (MockMvc)")
@Import({TestSecurityConfig.class, ApiExceptionHandler.class})
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AuthService authService;
    @MockitoBean
    TokenService tokenService;
    @MockitoBean
    SenhaResetService senhaResetService;
    @MockitoBean
    RefreshTokenService refreshTokenService;
    @MockitoBean
    UsuarioRepository usuarioRepository;
    @MockitoBean
    NotificacaoUsuarioRepository notificacaoRepository;
    @MockitoBean
    UsernameGeneratorService usernameGeneratorService;

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/auth/login
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/auth/login")
    class Login {

        @Test
        @DisplayName("deve retornar 200 com tokens ao logar com credenciais válidas")
        void deveRetornar200ComTokens() throws Exception {
            AuthRequest req = new AuthRequest();
            req.setUsuario("joao.silva");
            req.setSenha("Senha@123");

            AuthResponse resp = new AuthResponse("access-token", "refresh-token", "Bearer");
            when(authService.login(any())).thenReturn(resp);

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("access-token"))
                    .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                    .andExpect(jsonPath("$.tokenType").value("Bearer"));
        }

        @Test
        @DisplayName("deve retornar 400 quando credenciais inválidas")
        void deveRetornar400QuandoCredenciaisInvalidas() throws Exception {
            AuthRequest req = new AuthRequest();
            req.setUsuario("joao.silva");
            req.setSenha("errada");

            when(authService.login(any())).thenThrow(new CredenciaisInvalidasException("Credenciais inválidas"));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").exists());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/auth/cadastro
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/auth/cadastro")
    class Cadastro {

        @Test
        @DisplayName("deve retornar 200 com mensagem de sucesso ao cadastrar usuário válido")
        void deveRetornar200AoCadastrar() throws Exception {
            RegisterRequest req = new RegisterRequest();
            req.setNomeCompleto("João Silva");
            req.setEmail("joao@email.com");
            req.setSenha("Senha@123");
            req.setDataNascimento(LocalDate.of(1990, 1, 1));

            doNothing().when(authService).cadastrar(any());

            mockMvc.perform(post("/api/auth/cadastro")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Cadastro realizado com sucesso"));
        }

        @Test
        @DisplayName("deve retornar 400 quando email já cadastrado")
        void deveRetornar400QuandoEmailJaCadastrado() throws Exception {
            RegisterRequest req = new RegisterRequest();
            req.setNomeCompleto("João Silva");
            req.setEmail("joao@email.com");
            req.setSenha("Senha@123");
            req.setDataNascimento(LocalDate.of(1990, 1, 1));

            doThrow(new UsuarioException("E-mail já cadastrado"))
                    .when(authService).cadastrar(any());

            mockMvc.perform(post("/api/auth/cadastro")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andDo(print())
                    .andExpect(result -> {
                        System.out.println(result.getResponse().getContentAsString());
                    })
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("E-mail já cadastrado"));
        }

        @Test
        @DisplayName("deve retornar 400 quando senha fraca")
        void deveRetornar400QuandoSenhaFraca() throws Exception {
            RegisterRequest req = new RegisterRequest();
            req.setNomeCompleto("João Silva");
            req.setEmail("joao@email.com");
            req.setSenha("fraca");
            req.setDataNascimento(LocalDate.of(1990, 1, 1));

            doThrow(new SenhaFracaException("Senha fraca"))
                    .when(authService).cadastrar(any());

            mockMvc.perform(post("/api/auth/cadastro")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").exists());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/auth/username-preview
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/auth/username-preview")
    class UsernamePreview {

        @Test
        @DisplayName("deve retornar username gerado para nome válido")
        void deveRetornarUsernameGerado() throws Exception {
            when(usernameGeneratorService.gerar("Francisco Viana")).thenReturn("francisco.viana");

            mockMvc.perform(get("/api/auth/username-preview")
                            .param("nome", "Francisco Viana"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("francisco.viana"));
        }

        @Test
        @DisplayName("deve retornar username vazio para nome muito curto")
        void deveRetornarVazioParaNomeCurto() throws Exception {
            mockMvc.perform(get("/api/auth/username-preview")
                            .param("nome", "Jo"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(""));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/auth/refresh
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/auth/refresh")
    class Refresh {

        @Test
        @DisplayName("deve retornar novo accessToken com refreshToken válido")
        void deveRetornarNovoToken() throws Exception {
            Usuario usuario = Usuario.builder()
                    .username("joao.silva")
                    .nome("joao.silva")
                    .email("joao@email.com")
                    .senha("hash")
                    .roles(Set.of("USER"))
                    .createdAt(Instant.now())
                    .ultimaAlteracaoSenha(Instant.now())
                    .build();

            RefreshToken refreshToken = RefreshToken.builder()
                    .token("refresh-token-valido")
                    .usuario(usuario)
                    .expiryDate(Instant.now().plusSeconds(3600))
                    .build();

            when(refreshTokenService.validate("refresh-token-valido")).thenReturn(refreshToken);
            when(tokenService.generateToken("joao.silva", Set.of("USER"))).thenReturn("novo-access-token");

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"refreshToken\":\"refresh-token-valido\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("novo-access-token"));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/auth/esqueci-senha
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("POST /api/auth/esqueci-senha deve retornar 200 sempre (sem revelar existência)")
    void deveRetornar200EmEsqueciSenha() throws Exception {
        doNothing().when(senhaResetService).createPasswordResetTokenForEmail(any());

        mockMvc.perform(post("/api/auth/esqueci-senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"qualquer@email.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DELETE /api/auth/usuarios/{id}
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("DELETE /api/auth/usuarios/{id} deve retornar 200 ao excluir com sucesso")
    @WithMockUser(roles = "ADMIN")
    void deveExcluirUsuario() throws Exception {
        java.util.UUID id = java.util.UUID.randomUUID();
        doNothing().when(authService).excluirUsuario(id);

        mockMvc.perform(delete("/api/auth/usuarios/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Usuário excluído com sucesso"));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/auth/reset-senha
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/auth/reset-senha")
    class ResetSenha {

        @Test
        @DisplayName("deve retornar 200 quando token válido e senha atualizada")
        void deveRetornar200QuandoSucesso() throws Exception {
            when(senhaResetService.resetPassword(any(), any())).thenReturn(true);

            mockMvc.perform(post("/api/auth/reset-senha")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"token\":\"token-valido\",\"novaSenha\":\"NovaSenha@123\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Senha atualizada"));
        }

        @Test
        @DisplayName("deve retornar 400 quando token inválido ou expirado")
        void deveRetornar400QuandoTokenInvalido() throws Exception {
            when(senhaResetService.resetPassword(any(), any())).thenReturn(false);

            mockMvc.perform(post("/api/auth/reset-senha")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"token\":\"token-invalido\",\"novaSenha\":\"NovaSenha@123\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Token inválido ou expirado"));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/auth/usuarios
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("GET /api/auth/usuarios deve retornar página de usuários")
    @WithMockUser
    void deveListarUsuarios() throws Exception {
        when(authService.listarUsuarios(any())).thenReturn(
                new org.springframework.data.domain.PageImpl<>(java.util.Collections.emptyList()));

        mockMvc.perform(get("/api/auth/usuarios"))
                .andExpect(status().isOk());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // PUT /api/auth/usuarios/{id}
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("PUT /api/auth/usuarios/{id} deve retornar 200 ao atualizar")
    @WithMockUser
    void deveAtualizarUsuario() throws Exception {
        java.util.UUID id = java.util.UUID.randomUUID();
        doNothing().when(authService).atualizarUsuario(eq(id), any());

        mockMvc.perform(put("/api/auth/usuarios/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Usuário atualizado com sucesso"));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/auth/username-preview com exceção
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("GET /api/auth/username-preview deve retornar erro quando gerar lança exceção")
    void deveRetornarErroCasoDeFalha() throws Exception {
        when(usernameGeneratorService.gerar(any())).thenThrow(new RuntimeException("Falha interna"));

        mockMvc.perform(get("/api/auth/username-preview")
                        .param("nome", "Francisco Viana"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Falha interna"));
    }
}
