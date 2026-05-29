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
import io.github.franciscosviana.stmservicos.config.TestSecurityConfig;
import io.github.franciscosviana.stmservicos.domain.model.HistoricoSenha;
import io.github.franciscosviana.stmservicos.domain.model.RefreshToken;
import io.github.franciscosviana.stmservicos.domain.model.Usuario;
import io.github.franciscosviana.stmservicos.domain.repository.HistoricoSenhaRepository;
import io.github.franciscosviana.stmservicos.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("AuthService")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private EmailService emailService;
    @Mock
    private TokenService tokenService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private HistoricoSenhaRepository historicoSenhaRepository;
    @Mock
    private UsernameGeneratorService usernameGeneratorService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "frontendUrl", "http://localhost:4200");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // LOGIN
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("deve retornar AuthResponse com tokens ao fazer login com usuário válido")
        void deveRetornarTokensComLoginValido() {
            var usuario = usuarioComRoles("joao.silva", Set.of("USER"));
            var refreshToken = RefreshToken.builder()
                    .token("refresh-abc")
                    .usuario(usuario)
                    .expiryDate(Instant.now().plusSeconds(3600))
                    .build();

            when(usuarioRepository.findByUsername("joao.silva")).thenReturn(Optional.of(usuario));
            when(tokenService.generateToken("joao.silva", Set.of("USER"))).thenReturn("access-token-xyz");
            when(refreshTokenService.create(usuario)).thenReturn(refreshToken);

            AuthRequest req = new AuthRequest();
            req.setUsuario("joao.silva");
            req.setSenha("Senha@123");

            AuthResponse resp = authService.login(req);

            assertThat(resp.getAccessToken()).isEqualTo("access-token-xyz");
            assertThat(resp.getRefreshToken()).isEqualTo("refresh-abc");
            assertThat(resp.getTokenType()).isEqualTo("Bearer");

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        @DisplayName("deve tentar buscar por email quando username não encontrado")
        void deveBuscarPorEmailQuandoUsernameNaoEncontrado() {
            var usuario = usuarioComRoles("joao.silva", Set.of("USER"));
            var refreshToken = RefreshToken.builder()
                    .token("refresh-abc")
                    .usuario(usuario)
                    .expiryDate(Instant.now().plusSeconds(3600))
                    .build();

            when(usuarioRepository.findByUsername("joao@email.com")).thenReturn(Optional.empty());
            when(usuarioRepository.findByEmail("joao@email.com")).thenReturn(Optional.of(usuario));
            when(tokenService.generateToken(any(), any())).thenReturn("token");
            when(refreshTokenService.create(usuario)).thenReturn(refreshToken);

            AuthRequest req = new AuthRequest();
            req.setUsuario("joao@email.com");
            req.setSenha("Senha@123");

            AuthResponse resp = authService.login(req);
            assertThat(resp).isNotNull();
        }

        @Test
        @DisplayName("deve lançar UsuarioException quando usuário não encontrado")
        void deveLancarExcecaoQuandoUsuarioNaoExiste() {
            when(usuarioRepository.findByUsername(any())).thenReturn(Optional.empty());
            when(usuarioRepository.findByEmail(any())).thenReturn(Optional.empty());

            AuthRequest req = new AuthRequest();
            req.setUsuario("inexistente");
            req.setSenha("Senha@123");

            assertThatThrownBy(() -> authService.login(req))
                    .isInstanceOf(UsuarioException.class)
                    .hasMessageContaining("não encontrado");
        }

        @Test
        @DisplayName("deve propagar BadCredentialsException do authenticationManager")
        void devePropgarExcecaoDeCredenciaisInvalidas() {
            doThrow(new BadCredentialsException("Bad credentials"))
                    .when(authenticationManager).authenticate(any());

            AuthRequest req = new AuthRequest();
            req.setUsuario("joao.silva");
            req.setSenha("senha-errada");

            assertThatThrownBy(() -> authService.login(req))
                    .isInstanceOf(BadCredentialsException.class);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // CADASTRO
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("cadastrar()")
    class Cadastrar {

        @Test
        @DisplayName("deve salvar usuário e histórico de senha com sucesso")
        void deveSalvarUsuarioComSucesso() {
            RegisterRequest req = registroValido();

            when(usuarioRepository.existsByEmail(req.getEmail())).thenReturn(false);
            when(usernameGeneratorService.gerar(req.getNomeCompleto())).thenReturn("joao.silva");
            when(passwordEncoder.encode(req.getSenha())).thenReturn("hash123");

            authService.cadastrar(req);

            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
            verify(usuarioRepository).save(captor.capture());
            assertThat(captor.getValue().getUsername()).isEqualTo("joao.silva");
            assertThat(captor.getValue().getEmail()).isEqualTo(req.getEmail());

            verify(historicoSenhaRepository).save(any(HistoricoSenha.class));
        }

        @Test
        @DisplayName("deve lançar UsuarioException quando email já cadastrado")
        void deveLancarExcecaoSeEmailJaCadastrado() {
            RegisterRequest req = registroValido();
            when(usuarioRepository.existsByEmail(req.getEmail())).thenReturn(true);

            assertThatThrownBy(() -> authService.cadastrar(req))
                    .isInstanceOf(UsuarioException.class)
                    .hasMessageContaining("E-mail já cadastrado");
        }

        @Test
        @DisplayName("deve lançar SenhaFracaException quando senha fraca")
        void deveLancarExcecaoSeSenhaFraca() {
            RegisterRequest req = registroValido();
            req.setSenha("fraca");

            assertThatThrownBy(() -> authService.cadastrar(req))
                    .isInstanceOf(SenhaFracaException.class);
        }

        @Test
        @DisplayName("deve lançar RoleException quando role inválida informada")
        void deveLancarExcecaoSeRoleInvalida() {
            RegisterRequest req = registroValido();
            req.setRoles(Set.of("SUPERUSER"));
            when(usuarioRepository.existsByEmail(req.getEmail())).thenReturn(false);

            assertThatThrownBy(() -> authService.cadastrar(req))
                    .isInstanceOf(RoleException.class)
                    .hasMessageContaining("Role inválida");
        }

        @Test
        @DisplayName("deve definir role USER quando nenhuma role informada")
        void deveDefinirRoleUserPorPadrao() {
            RegisterRequest req = registroValido();
            req.setRoles(null);

            when(usuarioRepository.existsByEmail(req.getEmail())).thenReturn(false);
            when(usernameGeneratorService.gerar(any())).thenReturn("joao.silva");
            when(passwordEncoder.encode(any())).thenReturn("hash");

            authService.cadastrar(req);

            ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
            verify(usuarioRepository).save(captor.capture());
            assertThat(captor.getValue().getRoles()).containsExactly("USER");
        }

        @Test
        @DisplayName("não deve lançar exceção se e-mail de boas-vindas falhar")
        void naoDeveFalharSeEmailFalhar() {
            RegisterRequest req = registroValido();
            when(usuarioRepository.existsByEmail(req.getEmail())).thenReturn(false);
            when(usernameGeneratorService.gerar(any())).thenReturn("joao.silva");
            when(passwordEncoder.encode(any())).thenReturn("hash");
            doThrow(new RuntimeException("Brevo indisponível"))
                    .when(emailService).enviarEmail(any(), any(), any());

            // Não deve propagar a exceção de email
            authService.cadastrar(req);

            verify(usuarioRepository).save(any());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // ATUALIZAÇÃO
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("atualizarUsuario()")
    class AtualizarUsuario {

        @Test
        @DisplayName("deve atualizar campos do usuário com sucesso")
        void deveAtualizarUsuario() {
            UUID id = UUID.randomUUID();
            Usuario usuario = usuarioComRoles("joao.silva", Set.of("USER"));
            usuario.setId(id);
            usuario.setEmail("joao@email.com");

            UpdateUsuarioRequest req = new UpdateUsuarioRequest();
            req.setNomeCompleto("João Atualizado");
            req.setEmail("novo@email.com");

            when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.existsByEmail("novo@email.com")).thenReturn(false);

            authService.atualizarUsuario(id, req);

            assertThat(usuario.getNomeCompleto()).isEqualTo("João Atualizado");
            assertThat(usuario.getEmail()).isEqualTo("novo@email.com");
            verify(usuarioRepository).save(usuario);
        }

        @Test
        @DisplayName("deve lançar UsuarioException se novo email já pertence a outro usuário")
        void deveLancarExcecaoSeEmailJaUsado() {
            UUID id = UUID.randomUUID();
            Usuario usuario = usuarioComRoles("joao.silva", Set.of("USER"));
            usuario.setId(id);
            usuario.setEmail("joao@email.com");

            UpdateUsuarioRequest req = new UpdateUsuarioRequest();
            req.setEmail("outro@email.com");

            when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));
            when(usuarioRepository.existsByEmail("outro@email.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.atualizarUsuario(id, req))
                    .isInstanceOf(UsuarioException.class)
                    .hasMessageContaining("E-mail já cadastrado");
        }

        @Test
        @DisplayName("deve lançar UsuarioException se usuário não encontrado")
        void deveLancarExcecaoSeUsuarioNaoEncontrado() {
            UUID id = UUID.randomUUID();
            when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.atualizarUsuario(id, new UpdateUsuarioRequest()))
                    .isInstanceOf(UsuarioException.class);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // ATUALIZAÇÃO - branches adicionais
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("atualizarUsuario() branches adicionais")
    class AtualizarUsuarioBranches {

        @Test
        @DisplayName("deve atualizar roles quando informadas e válidas")
        void deveAtualizarRoles() {
            UUID id = UUID.randomUUID();
            Usuario usuario = usuarioComRoles("joao.silva", java.util.Set.of("USER"));
            usuario.setId(id);
            usuario.setEmail("joao@email.com");

            UpdateUsuarioRequest req = new UpdateUsuarioRequest();
            req.setRoles(java.util.Set.of("ADMIN"));

            when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));

            authService.atualizarUsuario(id, req);

            assertThat(usuario.getRoles()).containsExactly("ADMIN");
            verify(usuarioRepository).save(usuario);
        }

        @Test
        @DisplayName("deve lançar RoleException quando role inválida na atualização")
        void deveLancarExcecaoRoleInvalidaNaAtualizacao() {
            UUID id = UUID.randomUUID();
            Usuario usuario = usuarioComRoles("joao.silva", java.util.Set.of("USER"));
            usuario.setId(id);

            UpdateUsuarioRequest req = new UpdateUsuarioRequest();
            req.setRoles(java.util.Set.of("SUPERADMIN"));

            when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));

            assertThatThrownBy(() -> authService.atualizarUsuario(id, req))
                    .isInstanceOf(io.github.franciscosviana.stmservicos.common.validation.RoleException.class)
                    .hasMessageContaining("Role inválida");
        }

        @Test
        @DisplayName("deve atualizar dataNascimento quando informada")
        void deveAtualizarDataNascimento() {
            UUID id = UUID.randomUUID();
            Usuario usuario = usuarioComRoles("joao.silva", java.util.Set.of("USER"));
            usuario.setId(id);

            java.time.LocalDate nascimento = java.time.LocalDate.of(1990, 5, 15);
            UpdateUsuarioRequest req = new UpdateUsuarioRequest();
            req.setDataNascimento(nascimento);

            when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));

            authService.atualizarUsuario(id, req);

            assertThat(usuario.getDataNascimento()).isEqualTo(nascimento);
        }

        @Test
        @DisplayName("não deve atualizar campos quando request tem todos nulos")
        void naoDeveAtualizarQuandoTudoNulo() {
            UUID id = UUID.randomUUID();
            Usuario usuario = usuarioComRoles("joao.silva", java.util.Set.of("USER"));
            usuario.setId(id);
            usuario.setNomeCompleto("Nome Original");

            UpdateUsuarioRequest req = new UpdateUsuarioRequest();

            when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));

            authService.atualizarUsuario(id, req);

            assertThat(usuario.getNomeCompleto()).isEqualTo("Nome Original");
            verify(usuarioRepository).save(usuario);
        }

        @Test
        @DisplayName("não deve verificar email duplicado quando email não muda")
        void naoDeveVerificarEmailQuandoNaoMuda() {
            UUID id = UUID.randomUUID();
            Usuario usuario = usuarioComRoles("joao.silva", java.util.Set.of("USER"));
            usuario.setId(id);
            usuario.setEmail("joao@email.com");

            UpdateUsuarioRequest req = new UpdateUsuarioRequest();
            req.setEmail("joao@email.com"); // mesmo email

            when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));

            authService.atualizarUsuario(id, req);

            verify(usuarioRepository, never()).existsByEmail(any());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // EXCLUSÃO
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("excluirUsuario()")
    class ExcluirUsuario {

        @Test
        @DisplayName("deve excluir usuário e histórico de senha")
        void deveExcluirUsuario() {
            UUID id = UUID.randomUUID();
            Usuario usuario = usuarioComRoles("joao.silva", Set.of("USER"));
            usuario.setId(id);

            when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));

            authService.excluirUsuario(id);

            verify(historicoSenhaRepository).deleteAllByUsuarioId(usuario);
            verify(usuarioRepository).delete(usuario);
        }

        @Test
        @DisplayName("deve lançar UsuarioException se usuário não encontrado")
        void deveLancarExcecaoSeNaoEncontrado() {
            UUID id = UUID.randomUUID();
            when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.excluirUsuario(id))
                    .isInstanceOf(UsuarioException.class);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // VALIDAÇÕES DE SENHA
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("validarSenhaForte()")
    class ValidarSenhaForte {

        @Test
        @DisplayName("deve aceitar senha forte válida")
        void deveAceitarSenhaForte() {
            // Não lança exceção
            authService.validarSenhaForte("Senha@123");
        }

        @Test
        @DisplayName("deve rejeitar senha sem caractere especial")
        void deveRejeitarSenhaSemEspecial() {
            assertThatThrownBy(() -> authService.validarSenhaForte("Senha1234"))
                    .isInstanceOf(SenhaFracaException.class);
        }

        @Test
        @DisplayName("deve rejeitar senha sem letra maiúscula")
        void deveRejeitarSenhaSemMaiuscula() {
            assertThatThrownBy(() -> authService.validarSenhaForte("senha@123"))
                    .isInstanceOf(SenhaFracaException.class);
        }

        @Test
        @DisplayName("deve rejeitar senha sem letra minúscula")
        void deveRejeitarSenhaSemMinuscula() {
            assertThatThrownBy(() -> authService.validarSenhaForte("SENHA@123"))
                    .isInstanceOf(SenhaFracaException.class);
        }

        @Test
        @DisplayName("deve rejeitar senha com menos de 8 caracteres")
        void deveRejeitarSenhasCurtas() {
            assertThatThrownBy(() -> authService.validarSenhaForte("S@1a"))
                    .isInstanceOf(SenhaFracaException.class);
        }

        @Test
        @DisplayName("deve rejeitar senha nula")
        void deveRejeitarSenhaNula() {
            assertThatThrownBy(() -> authService.validarSenhaForte(null))
                    .isInstanceOf(SenhaFracaException.class);
        }
    }

    @Nested
    @DisplayName("validarUltimasSenhas()")
    class ValidarUltimasSenhas {

        @Test
        @DisplayName("deve lançar SenhaRepetidaException se nova senha coincidir com histórico")
        void deveLancarExcecaoSeSenhaRepetida() {
            Usuario usuario = usuarioComRoles("joao.silva", Set.of("USER"));
            HistoricoSenha hist = HistoricoSenha.builder()
                    .usuarioId(usuario).senhaHash("hash-antigo").build();

            when(historicoSenhaRepository.findTop5ByUsuarioIdOrderByCriadaEmDesc(usuario))
                    .thenReturn(List.of(hist));
            when(passwordEncoder.matches("Senha@123", "hash-antigo")).thenReturn(true);

            assertThatThrownBy(() -> authService.validarUltimasSenhas(usuario, "Senha@123"))
                    .isInstanceOf(SenhaRepetidaException.class);
        }

        @Test
        @DisplayName("deve passar se nova senha não constar no histórico")
        void devePassarSeSenhaNova() {
            Usuario usuario = usuarioComRoles("joao.silva", Set.of("USER"));
            HistoricoSenha hist = HistoricoSenha.builder()
                    .usuarioId(usuario).senhaHash("hash-antigo").build();

            when(historicoSenhaRepository.findTop5ByUsuarioIdOrderByCriadaEmDesc(usuario))
                    .thenReturn(List.of(hist));
            when(passwordEncoder.matches("NovaSenha@456", "hash-antigo")).thenReturn(false);

            // Não deve lançar exceção
            authService.validarUltimasSenhas(usuario, "NovaSenha@456");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // LISTAR
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("listarUsuarios() deve retornar página mapeada para UsuarioResponse")
    void deveListarUsuariosPaginados() {
        Usuario u = usuarioComRoles("joao.silva", Set.of("USER"));
        u.setId(UUID.randomUUID());
        u.setEmail("joao@email.com");

        Page<Usuario> page = new PageImpl<>(List.of(u));
        when(usuarioRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        Page<UsuarioResponse> resultado = authService.listarUsuarios(PageRequest.of(0, 10));

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getUsername()).isEqualTo("joao.silva");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────
    private Usuario usuarioComRoles(String username, Set<String> roles) {
        return Usuario.builder()
                .username(username)
                .nome(username)
                .nomeCompleto("João Silva")
                .email(username + "@email.com")
                .senha("hash")
                .roles(roles)
                .enabled(true)
                .createdAt(Instant.now())
                .ultimaAlteracaoSenha(Instant.now())
                .build();
    }

    private RegisterRequest registroValido() {
        RegisterRequest req = new RegisterRequest();
        req.setNomeCompleto("João Silva");
        req.setEmail("joao@email.com");
        req.setSenha("Senha@123");
        req.setRoles(Set.of("USER"));
        return req;
    }
}
