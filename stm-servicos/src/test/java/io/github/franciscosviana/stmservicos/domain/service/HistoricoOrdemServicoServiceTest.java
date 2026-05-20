package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.assembler.HistoricoOrdemServicoOutputAssembler;
import io.github.franciscosviana.stmservicos.api.model.output.HistoricoOrdemServicoOutput;
import io.github.franciscosviana.stmservicos.domain.model.HistoricoOrdemServico;
import io.github.franciscosviana.stmservicos.domain.model.OrdemServico;
import io.github.franciscosviana.stmservicos.domain.model.Usuario;
import io.github.franciscosviana.stmservicos.domain.model.enums.TipoAcaoOS;
import io.github.franciscosviana.stmservicos.domain.repository.HistoricoOrdemServicoRepository;
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

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("HistoricoOrdemServicoService")
@ExtendWith(MockitoExtension.class)
class HistoricoOrdemServicoServiceTest {

    @Mock
    HistoricoOrdemServicoRepository repository;

    @Mock
    UsuarioAutenticadoService usuarioAutenticadoService;

    @Mock
    HistoricoOrdemServicoOutputAssembler assembler;

    @InjectMocks
    HistoricoOrdemServicoService service;

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

    @Nested
    @DisplayName("registrar()")
    class Registrar {

        @Test
        @DisplayName("deve salvar histórico com os dados corretos")
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
        @DisplayName("deve registrar com TipoAcaoOS.ATUALIZACAO")
        void deveRegistrarAtualizacao() {
            when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuario);

            service.registrar(os, TipoAcaoOS.ATUALIZACAO, "Campo X alterado");

            ArgumentCaptor<HistoricoOrdemServico> captor =
                    ArgumentCaptor.forClass(HistoricoOrdemServico.class);
            verify(repository).save(captor.capture());

            assertThat(captor.getValue().getAcao()).isEqualTo(TipoAcaoOS.ATUALIZACAO);
        }

        @Test
        @DisplayName("deve registrar com TipoAcaoOS.CONCLUSAO_ORDEM")
        void deveRegistrarConclusao() {
            when(usuarioAutenticadoService.getUsuarioLogado()).thenReturn(usuario);

            service.registrar(os, TipoAcaoOS.CONCLUSAO_ORDEM, "OS concluída");

            ArgumentCaptor<HistoricoOrdemServico> captor =
                    ArgumentCaptor.forClass(HistoricoOrdemServico.class);
            verify(repository).save(captor.capture());

            assertThat(captor.getValue().getAcao()).isEqualTo(TipoAcaoOS.CONCLUSAO_ORDEM);
        }
    }

    @Nested
    @DisplayName("deletarPorOrdemServico()")
    class DeletarPorOrdemServico {

        @Test
        @DisplayName("deve chamar deleteByOrdemServicoId com o ID correto")
        void deveDeletar() {
            UUID osId = UUID.randomUUID();
            service.deletarPorOrdemServico(osId);
            verify(repository).deleteByOrdemServicoId(osId);
        }
    }

    @Nested
    @DisplayName("listarPorOrdemServico()")
    class ListarPorOrdemServico {

        @Test
        @DisplayName("deve retornar lista mapeada")
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

        @Test
        @DisplayName("deve retornar lista vazia quando não há histórico")
        void deveRetornarListaVazia() {
            UUID osId = UUID.randomUUID();
            when(repository.findByOrdemServicoIdOrderByDataHoraAsc(osId))
                    .thenReturn(List.of());

            List<HistoricoOrdemServicoOutput> result = service.listarPorOrdemServico(osId);

            assertThat(result).isEmpty();
        }
    }
}
