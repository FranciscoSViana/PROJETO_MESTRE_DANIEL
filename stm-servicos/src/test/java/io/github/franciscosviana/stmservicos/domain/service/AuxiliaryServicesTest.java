package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.assembler.HistoricoOrdemServicoOutputAssembler;
import io.github.franciscosviana.stmservicos.api.model.output.EstadoOutput;
import io.github.franciscosviana.stmservicos.api.model.output.HistoricoOrdemServicoOutput;
import io.github.franciscosviana.stmservicos.api.model.output.MunicipioOutput;
import io.github.franciscosviana.stmservicos.common.client.BrasilAPIClient;
import io.github.franciscosviana.stmservicos.common.client.ViaCepClient;
import io.github.franciscosviana.stmservicos.common.client.model.EstadoResponse;
import io.github.franciscosviana.stmservicos.common.client.model.MunicipioResponse;
import io.github.franciscosviana.stmservicos.common.client.model.ViaCepResponse;
import io.github.franciscosviana.stmservicos.domain.model.*;
import io.github.franciscosviana.stmservicos.domain.model.enums.TipoAcaoOS;
import io.github.franciscosviana.stmservicos.domain.repository.*;
import io.github.franciscosviana.stmservicos.domain.service.helper.UsuarioAutenticadoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Services auxiliares")
class AuxiliaryServicesTest {

    // ──────────────────────────────────────────────────────────────────────────
    // UsuarioDetailsService
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("UsuarioDetailsService")
    @ExtendWith(MockitoExtension.class)
    class UsuarioDetailsServiceTest {

        @Mock UsuarioRepository usuarioRepository;
        @InjectMocks UsuarioDetailsService service;

        @Test
        @DisplayName("loadUserByUsername() deve retornar UserDetails quando usuário encontrado")
        void deveCarregarUsuario() {
            Usuario usuario = Usuario.builder()
                    .nome("joao.silva")
                    .username("joao.silva")
                    .senha("$2a$hash")
                    .roles(Set.of("USER"))
                    .enabled(true)
                    .createdAt(Instant.now())
                    .ultimaAlteracaoSenha(Instant.now())
                    .build();

            when(usuarioRepository.findByNome("joao.silva")).thenReturn(Optional.of(usuario));

            UserDetails details = service.loadUserByUsername("joao.silva");

            assertThat(details.getUsername()).isEqualTo("joao.silva");
            assertThat(details.getPassword()).isEqualTo("$2a$hash");
            assertThat(details.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("loadUserByUsername() deve lançar UsernameNotFoundException quando não encontrado")
        void deveLancarExcecaoSeNaoEncontrado() {
            when(usuarioRepository.findByNome("inexistente")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.loadUserByUsername("inexistente"))
                    .isInstanceOf(UsernameNotFoundException.class);
        }

        @Test
        @DisplayName("deve retornar authorities com prefixo ROLE_")
        void deveRetornarRoles() {
            Usuario usuario = Usuario.builder()
                    .nome("admin")
                    .username("admin")
                    .senha("hash")
                    .roles(Set.of("ADMIN"))
                    .enabled(true)
                    .createdAt(Instant.now())
                    .ultimaAlteracaoSenha(Instant.now())
                    .build();

            when(usuarioRepository.findByNome("admin")).thenReturn(Optional.of(usuario));

            UserDetails details = service.loadUserByUsername("admin");

            assertThat(details.getAuthorities())
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // HistoricoOrdemServicoService
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("HistoricoOrdemServicoService")
    @ExtendWith(MockitoExtension.class)
    class HistoricoOrdemServicoServiceTest {

        @Mock HistoricoOrdemServicoRepository repository;
        @Mock UsuarioAutenticadoService usuarioAutenticadoService;
        @Mock HistoricoOrdemServicoOutputAssembler assembler;
        @InjectMocks HistoricoOrdemServicoService service;

        private OrdemServico os;
        private Usuario usuario;

        @BeforeEach
        void setUp() {
            os = new OrdemServico();
            os.setId(UUID.randomUUID());
            os.setOsg("OSG260001");

            usuario = Usuario.builder()
                    .username("joao.silva").nome("joao.silva")
                    .email("j@e.com").senha("hash")
                    .roles(Set.of("USER")).enabled(true)
                    .createdAt(Instant.now()).ultimaAlteracaoSenha(Instant.now())
                    .build();
        }

        @Test
        @DisplayName("registrar() deve salvar histórico com os dados corretos")
        void deveRegistrarHistorico() {
            when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuario);

            service.registrar(os, TipoAcaoOS.CRIACAO, "OS criada");

            ArgumentCaptor<HistoricoOrdemServico> captor =
                    ArgumentCaptor.forClass(HistoricoOrdemServico.class);
            verify(repository).save(captor.capture());

            HistoricoOrdemServico salvo = captor.getValue();
            assertThat(salvo.getOrdemServico()).isEqualTo(os);
            assertThat(salvo.getAcao()).isEqualTo(TipoAcaoOS.CRIACAO);
            assertThat(salvo.getDescricao()).isEqualTo("OS criada");
            assertThat(salvo.getDataHora()).isNotNull();
        }

        @Test
        @DisplayName("deletarPorOrdemServico() deve chamar deleteByOrdemServicoId")
        void deveDeletar() {
            UUID osId = UUID.randomUUID();
            service.deletarPorOrdemServico(osId);
            verify(repository).deleteByOrdemServicoId(osId);
        }

        @Test
        @DisplayName("listarPorOrdemServico() deve retornar lista mapeada")
        void deveListar() {
            UUID osId = UUID.randomUUID();
            HistoricoOrdemServico h = new HistoricoOrdemServico();
            HistoricoOrdemServicoOutput output = new HistoricoOrdemServicoOutput();

            when(repository.findByOrdemServicoIdOrderByDataHoraAsc(osId))
                    .thenReturn(List.of(h));
            when(assembler.toModel(h)).thenReturn(output);

            List<HistoricoOrdemServicoOutput> result = service.listarPorOrdemServico(osId);

            assertThat(result).hasSize(1).contains(output);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // EnderecoService
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("EnderecoService")
    @ExtendWith(MockitoExtension.class)
    class EnderecoServiceTest {

        @Mock ViaCepClient viaCepClient;
        @InjectMocks EnderecoService service;

        @Test
        @DisplayName("consultarCep() deve delegar para ViaCepClient")
        void deveConsultarCep() {
            ViaCepResponse resp = new ViaCepResponse();
            when(viaCepClient.buscarEnderecoPorCep("64000-000")).thenReturn(resp);

            assertThat(service.consultarCep("64000-000")).isEqualTo(resp);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // IbgeService
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("IbgeService")
    @ExtendWith(MockitoExtension.class)
    class IbgeServiceTest {

        @Mock BrasilAPIClient brasilAPIClient;
        @InjectMocks IbgeService service;

        @Test
        @DisplayName("listarEstados() deve mapear EstadoResponse para EstadoOutput")
        void deveListarEstados() {
            EstadoResponse estado = new EstadoResponse();
            estado.setNome("Piauí");
            estado.setSigla("PI");

            when(brasilAPIClient.buscarEstados()).thenReturn(List.of(estado));

            List<EstadoOutput> result = service.listarEstados();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).nome()).isEqualTo("Piauí");
            assertThat(result.get(0).sigla()).isEqualTo("PI");
        }

        @Test
        @DisplayName("listarMunicipios() deve mapear MunicipioResponse para MunicipioOutput")
        void deveListarMunicipios() {
            MunicipioResponse municipio = new MunicipioResponse();
            municipio.setNome("Teresina");
            municipio.setCodigo_ibge("2211001");

            when(brasilAPIClient.buscarMunicipios("PI")).thenReturn(List.of(municipio));

            List<MunicipioOutput> result = service.listarMunicipios("pi");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).nome()).isEqualTo("Teresina");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // NotificacaoSenhaService
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("NotificacaoSenhaService")
    @ExtendWith(MockitoExtension.class)
    class NotificacaoSenhaServiceTest {

        @Mock UsuarioRepository usuarioRepository;
        @Mock NotificacaoUsuarioRepository notificacaoRepository;
        @Mock EmailService emailService;
        @InjectMocks NotificacaoSenhaService service;

        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(service, "frontendUrl", "http://localhost:4200");
        }

        @Test
        @DisplayName("registrarTrocaSenha() deve atualizar ultimaAlteracaoSenha e zerar flag")
        void deveRegistrarTrocaSenha() {
            Usuario usuario = Usuario.builder()
                    .username("joao").nome("joao").email("j@e.com").senha("h")
                    .roles(Set.of("USER")).enabled(true)
                    .createdAt(Instant.now())
                    .ultimaAlteracaoSenha(Instant.now().minus(100, ChronoUnit.DAYS))
                    .notificacaoSenhaEnviada(true)
                    .build();

            service.registrarTrocaSenha(usuario);

            assertThat(usuario.isNotificacaoSenhaEnviada()).isFalse();
            assertThat(usuario.getUltimaAlteracaoSenha())
                    .isAfter(Instant.now().minus(5, ChronoUnit.SECONDS));
            verify(usuarioRepository).save(usuario);
        }

        @Test
        @DisplayName("verificarSenhasAntigos() deve enviar notificação para usuários com senha antiga")
        void deveNotificarSenhaAntiga() {
            Usuario usuario = Usuario.builder()
                    .username("joao").nome("joao").nomeCompleto("João Silva")
                    .email("j@e.com").senha("h").roles(Set.of("USER")).enabled(true)
                    .createdAt(Instant.now())
                    .ultimaAlteracaoSenha(Instant.now().minus(91, ChronoUnit.DAYS))
                    .notificacaoSenhaEnviada(false)
                    .build();

            when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

            service.verificarSenhasAntigos();

            verify(notificacaoRepository).save(any(NotificacaoUsuario.class));
            verify(usuarioRepository, atLeastOnce()).save(usuario);
            assertThat(usuario.isNotificacaoSenhaEnviada()).isTrue();
        }

        @Test
        @DisplayName("verificarSenhasAntigos() não deve notificar usuários com senha recente")
        void naoDeveNotificarSenhaRecente() {
            Usuario usuario = Usuario.builder()
                    .username("joao").nome("joao").email("j@e.com").senha("h")
                    .roles(Set.of("USER")).enabled(true)
                    .createdAt(Instant.now())
                    .ultimaAlteracaoSenha(Instant.now().minus(10, ChronoUnit.DAYS))
                    .notificacaoSenhaEnviada(false)
                    .build();

            when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

            service.verificarSenhasAntigos();

            verify(notificacaoRepository, never()).save(any());
        }

        @Test
        @DisplayName("verificarSenhasAntigos() não deve reenviar se notificação já foi enviada")
        void naoDeveReenviarNotificacao() {
            Usuario usuario = Usuario.builder()
                    .username("joao").nome("joao").email("j@e.com").senha("h")
                    .roles(Set.of("USER")).enabled(true)
                    .createdAt(Instant.now())
                    .ultimaAlteracaoSenha(Instant.now().minus(100, ChronoUnit.DAYS))
                    .notificacaoSenhaEnviada(true) // já enviado
                    .build();

            when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

            service.verificarSenhasAntigos();

            verify(notificacaoRepository, never()).save(any());
        }
    }
}
