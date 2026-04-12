package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.assembler.SolucaoOSInputDisassembler;
import io.github.franciscosviana.stmservicos.api.assembler.SolucaoOSOutputAssembler;
import io.github.franciscosviana.stmservicos.api.model.input.SolucaoOSInput;
import io.github.franciscosviana.stmservicos.api.model.output.SolucaoOSOutput;
import io.github.franciscosviana.stmservicos.common.validation.OrdemServicoException;
import io.github.franciscosviana.stmservicos.domain.model.*;
import io.github.franciscosviana.stmservicos.domain.model.enums.StatusOrdem;
import io.github.franciscosviana.stmservicos.domain.model.enums.TipoAcaoOS;
import io.github.franciscosviana.stmservicos.domain.repository.OrdemServicoRepository;
import io.github.franciscosviana.stmservicos.domain.repository.PagamentoOSRepository;
import io.github.franciscosviana.stmservicos.domain.repository.SolucaoOSRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SolucaoService")
class SolucaoServiceTest {

    @Mock
    private SolucaoOSRepository solucaoOSRepository;
    @Mock
    private PagamentoOSRepository pagamentoOSRepository;
    @Mock
    private OrdemServicoRepository ordemServicoRepository;
    @Mock
    private SolucaoOSOutputAssembler solucaoOSOutputAssembler;
    @Mock
    private SolucaoOSInputDisassembler solucaoOSInputDisassembler;
    @Mock
    private HistoricoOrdemServicoService historicoOrdemServicoService;

    @InjectMocks
    private SolucaoService service;

    private UUID ordemId;
    private OrdemServico ordem;
    private Credenciado credenciado;

    @BeforeEach
    void setUp() {
        ordemId = UUID.randomUUID();

        credenciado = new Credenciado();
        credenciado.setValorChamado(new BigDecimal("120.00"));
        credenciado.setValorKm(new BigDecimal("3.00"));

        Cliente cliente = new Cliente();
        cliente.setNome("Cliente ABC");

        Contrato contrato = new Contrato();
        contrato.setId(UUID.randomUUID());

        ordem = new OrdemServico();
        ordem.setId(ordemId);
        ordem.setOsg("OSG260001");
        ordem.setOsClt("CLT-001");
        ordem.setCredenciado(credenciado);
        ordem.setCliente(cliente);
        ordem.setContrato(contrato);
        ordem.setStatus(StatusOrdem.ABERTA);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // finalizarOS()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("finalizarOS()")
    class FinalizarOS {

        @Test
        @DisplayName("deve finalizar OS, salvar solução, criar rascunho de pagamento e registrar histórico")
        void deveFinalizarComSucesso() {
            SolucaoOSInput input = inputSolucao();
            SolucaoOS solucao = solucaoComValores();

            when(ordemServicoRepository.findById(ordemId)).thenReturn(Optional.of(ordem));
            when(solucaoOSInputDisassembler.toDomainObject(input)).thenReturn(solucao);
            when(solucaoOSRepository.save(solucao)).thenReturn(solucao);
            when(pagamentoOSRepository.findByOrdemServicoId(ordemId)).thenReturn(Optional.empty());
            when(pagamentoOSRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(solucaoOSOutputAssembler.toModel(solucao)).thenReturn(new SolucaoOSOutput());

            service.finalizarOS(ordemId, input);

            assertThat(ordem.getStatus()).isEqualTo(StatusOrdem.CONCLUIDA);
            verify(solucaoOSRepository).save(solucao);
            verify(pagamentoOSRepository).save(any(PagamentoOS.class));
            verify(historicoOrdemServicoService).registrar(eq(ordem), eq(TipoAcaoOS.CONCLUSAO_ORDEM), any());
        }

        @Test
        @DisplayName("deve calcular valorTotal do rascunho corretamente com valores do credenciado")
        void deveCalcularRascunhoComValoresDoCredenciado() {
            SolucaoOSInput input = inputSolucao();
            SolucaoOS solucao = solucaoComValores();
            solucao.setKm(new BigDecimal("5"));
            solucao.setPedagio(new BigDecimal("4.00"));
            solucao.setEstacionamento(new BigDecimal("2.00"));
            solucao.setOutros(new BigDecimal("0.00"));

            when(ordemServicoRepository.findById(ordemId)).thenReturn(Optional.of(ordem));
            when(solucaoOSInputDisassembler.toDomainObject(input)).thenReturn(solucao);
            when(solucaoOSRepository.save(solucao)).thenReturn(solucao);
            when(pagamentoOSRepository.findByOrdemServicoId(ordemId)).thenReturn(Optional.empty());
            when(pagamentoOSRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(solucaoOSOutputAssembler.toModel(any())).thenReturn(new SolucaoOSOutput());

            service.finalizarOS(ordemId, input);

            // 120 + (5*3) + 4 + 2 + 0 = 141
            ArgumentCaptor<PagamentoOS> captor = ArgumentCaptor.forClass(PagamentoOS.class);
            verify(pagamentoOSRepository).save(captor.capture());
            assertThat(captor.getValue().getValorTotal()).isEqualByComparingTo("141.00");
            assertThat(captor.getValue().isPago()).isFalse();
        }

        @Test
        @DisplayName("deve usar ZERO quando credenciado é nulo")
        void deveUsarZeroQuandoSemCredenciado() {
            ordem.setCredenciado(null);

            SolucaoOSInput input = inputSolucao();
            SolucaoOS solucao = solucaoComValores();

            when(ordemServicoRepository.findById(ordemId)).thenReturn(Optional.of(ordem));
            when(solucaoOSInputDisassembler.toDomainObject(input)).thenReturn(solucao);
            when(solucaoOSRepository.save(solucao)).thenReturn(solucao);
            when(pagamentoOSRepository.findByOrdemServicoId(ordemId)).thenReturn(Optional.empty());
            when(pagamentoOSRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(solucaoOSOutputAssembler.toModel(any())).thenReturn(new SolucaoOSOutput());

            service.finalizarOS(ordemId, input);

            ArgumentCaptor<PagamentoOS> captor = ArgumentCaptor.forClass(PagamentoOS.class);
            verify(pagamentoOSRepository).save(captor.capture());
            assertThat(captor.getValue().getValorChamado()).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("deve ignorar criação de rascunho se PagamentoOS já existir")
        void deveIgnorarRascunhoSeJaExistir() {
            SolucaoOSInput input = inputSolucao();
            SolucaoOS solucao = solucaoComValores();

            when(ordemServicoRepository.findById(ordemId)).thenReturn(Optional.of(ordem));
            when(solucaoOSInputDisassembler.toDomainObject(input)).thenReturn(solucao);
            when(solucaoOSRepository.save(solucao)).thenReturn(solucao);
            when(pagamentoOSRepository.findByOrdemServicoId(ordemId))
                    .thenReturn(Optional.of(new PagamentoOS())); // já existe
            when(solucaoOSOutputAssembler.toModel(any())).thenReturn(new SolucaoOSOutput());

            service.finalizarOS(ordemId, input);

            verify(pagamentoOSRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve lançar OrdemServicoException quando OS não encontrada")
        void deveLancarExcecaoSeOsNaoEncontrada() {
            when(ordemServicoRepository.findById(ordemId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.finalizarOS(ordemId, inputSolucao()))
                    .isInstanceOf(OrdemServicoException.class)
                    .hasMessageContaining("OS não encontrada");
        }

        @Test
        @DisplayName("deve lançar OrdemServicoException quando OS já está concluída")
        void deveLancarExcecaoSeJaConcluida() {
            ordem.setStatus(StatusOrdem.CONCLUIDA);
            when(ordemServicoRepository.findById(ordemId)).thenReturn(Optional.of(ordem));

            assertThatThrownBy(() -> service.finalizarOS(ordemId, inputSolucao()))
                    .isInstanceOf(OrdemServicoException.class)
                    .hasMessageContaining("já está concluída");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // buscarPorId() e buscarPorOrdem()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("buscar()")
    class Buscar {

        @Test
        @DisplayName("buscarPorId() deve retornar output quando encontrado")
        void buscarPorIdDeveRetornar() {
            SolucaoOS solucao = new SolucaoOS();
            SolucaoOSOutput output = new SolucaoOSOutput();
            when(solucaoOSRepository.findById(1L)).thenReturn(Optional.of(solucao));
            when(solucaoOSOutputAssembler.toModel(solucao)).thenReturn(output);

            assertThat(service.buscarPorId(1L)).isEqualTo(output);
        }

        @Test
        @DisplayName("buscarPorId() deve lançar exceção quando não encontrado")
        void buscarPorIdDeveLancarExcecao() {
            when(solucaoOSRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPorId(99L))
                    .isInstanceOf(OrdemServicoException.class)
                    .hasMessageContaining("Solução não encontrada");
        }

        @Test
        @DisplayName("buscarPorOrdem() deve retornar output quando encontrado")
        void buscarPorOrdemDeveRetornar() {
            SolucaoOS solucao = new SolucaoOS();
            SolucaoOSOutput output = new SolucaoOSOutput();
            when(solucaoOSRepository.findByOrdemServicoId(ordemId)).thenReturn(Optional.of(solucao));
            when(solucaoOSOutputAssembler.toModel(solucao)).thenReturn(output);

            assertThat(service.buscarPorOrdem(ordemId)).isEqualTo(output);
        }

        @Test
        @DisplayName("buscarPorOrdem() deve lançar exceção quando não encontrado")
        void buscarPorOrdemDeveLancarExcecao() {
            when(solucaoOSRepository.findByOrdemServicoId(ordemId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPorOrdem(ordemId))
                    .isInstanceOf(OrdemServicoException.class)
                    .hasMessageContaining("Solução não encontrada para a OS");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────
    private SolucaoOSInput inputSolucao() {
        SolucaoOSInput input = new SolucaoOSInput();
        input.setSolucao("Substituição de peça X");
        input.setKm(new BigDecimal("0"));
        input.setPedagio(BigDecimal.ZERO);
        input.setEstacionamento(BigDecimal.ZERO);
        return input;
    }

    private SolucaoOS solucaoComValores() {
        SolucaoOS s = new SolucaoOS();
        s.setSolucao("Substituição de peça X");
        s.setKm(BigDecimal.ZERO);
        s.setPedagio(BigDecimal.ZERO);
        s.setEstacionamento(BigDecimal.ZERO);
        s.setOutros(BigDecimal.ZERO);
        return s;
    }
}
