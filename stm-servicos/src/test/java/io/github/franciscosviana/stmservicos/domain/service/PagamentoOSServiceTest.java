package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.assembler.PagamentoOSOutputAssembler;
import io.github.franciscosviana.stmservicos.api.model.input.PagamentoOSInput;
import io.github.franciscosviana.stmservicos.api.model.output.PagamentoOSOutput;
import io.github.franciscosviana.stmservicos.common.validation.OrdemServicoException;
import io.github.franciscosviana.stmservicos.domain.model.*;
import io.github.franciscosviana.stmservicos.domain.model.enums.TipoPagamento;
import io.github.franciscosviana.stmservicos.domain.repository.OrdemServicoRepository;
import io.github.franciscosviana.stmservicos.domain.repository.PagamentoOSRepository;
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
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PagamentoOSService")
class PagamentoOSServiceTest {

    @Mock
    private PagamentoOSRepository pagamentoOSRepository;
    @Mock
    private OrdemServicoRepository ordemServicoRepository;
    @Mock
    private PagamentoOSOutputAssembler pagamentoOSOutputAssembler;

    @InjectMocks
    private PagamentoOSService service;

    private UUID osId;
    private OrdemServico os;
    private SolucaoOS solucao;

    @BeforeEach
    void setUp() {
        osId = UUID.randomUUID();

        Credenciado credenciado = new Credenciado();
        credenciado.setValorChamado(new BigDecimal("100.00"));
        credenciado.setValorKm(new BigDecimal("2.50"));

        Cliente cliente = new Cliente();
        cliente.setNome("Empresa X");

        Contrato contrato = new Contrato();
        contrato.setId(UUID.randomUUID());

        solucao = new SolucaoOS();
        solucao.setOutros(new BigDecimal("0.00"));

        os = new OrdemServico();
        os.setId(osId);
        os.setOsg("OSG260001");
        os.setOsClt("OS-CLT-001");
        os.setCredenciado(credenciado);
        os.setCliente(cliente);
        os.setContrato(contrato);
        os.setSolucao(solucao);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // registrar()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("registrar()")
    class Registrar {

        @Test
        @DisplayName("deve registrar pagamento calculando valorTotal corretamente")
        void deveCalcularValorTotal() {
            PagamentoOSInput input = inputPagamento();
            input.setKm(new BigDecimal("10"));
            input.setPedagio(new BigDecimal("5.00"));
            input.setEstacionamento(new BigDecimal("3.00"));
            input.setValorOutros(new BigDecimal("2.00"));
            // valorTotal = 100 + (10*2.5) + 5 + 3 + 2 = 135

            PagamentoOS rascunho = new PagamentoOS();
            PagamentoOSOutput expectedOutput = new PagamentoOSOutput();

            when(ordemServicoRepository.buscarCompleta(osId)).thenReturn(Optional.of(os));
            when(pagamentoOSRepository.findByOrdemServicoId(osId)).thenReturn(Optional.of(rascunho));
            when(pagamentoOSRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(pagamentoOSOutputAssembler.toModel(any())).thenReturn(expectedOutput);

            service.registrar(osId, input);

            ArgumentCaptor<PagamentoOS> captor = ArgumentCaptor.forClass(PagamentoOS.class);
            verify(pagamentoOSRepository).save(captor.capture());

            PagamentoOS salvo = captor.getValue();
            assertThat(salvo.getValorTotal()).isEqualByComparingTo("135.00");
            assertThat(salvo.isPago()).isTrue();
        }

        @Test
        @DisplayName("deve usar valores do credenciado quando input não informa valorChamado/valorKm")
        void deveUsarValoresDoCredenciado() {
            PagamentoOSInput input = inputPagamento();
            input.setValorChamado(null);  // não informado → usa do credenciado (100)
            input.setValorKm(null);       // não informado → usa do credenciado (2.50)
            input.setKm(new BigDecimal("10"));

            PagamentoOS rascunho = new PagamentoOS();
            when(ordemServicoRepository.buscarCompleta(osId)).thenReturn(Optional.of(os));
            when(pagamentoOSRepository.findByOrdemServicoId(osId)).thenReturn(Optional.of(rascunho));
            when(pagamentoOSRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(pagamentoOSOutputAssembler.toModel(any())).thenReturn(new PagamentoOSOutput());

            service.registrar(osId, input);

            ArgumentCaptor<PagamentoOS> captor = ArgumentCaptor.forClass(PagamentoOS.class);
            verify(pagamentoOSRepository).save(captor.capture());

            // 100 + (10 * 2.5) = 125
            assertThat(captor.getValue().getValorTotal()).isEqualByComparingTo("125.00");
        }

        @Test
        @DisplayName("deve usar ZERO quando credenciado não tem valores e input não informa")
        void deveUsarZeroQuandoSemValores() {
            os.setCredenciado(null); // sem credenciado
            PagamentoOSInput input = inputPagamento();
            input.setValorChamado(null);
            input.setValorKm(null);
            input.setKm(null);

            PagamentoOS rascunho = new PagamentoOS();
            when(ordemServicoRepository.buscarCompleta(osId)).thenReturn(Optional.of(os));
            when(pagamentoOSRepository.findByOrdemServicoId(osId)).thenReturn(Optional.of(rascunho));
            when(pagamentoOSRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(pagamentoOSOutputAssembler.toModel(any())).thenReturn(new PagamentoOSOutput());

            service.registrar(osId, input);

            ArgumentCaptor<PagamentoOS> captor = ArgumentCaptor.forClass(PagamentoOS.class);
            verify(pagamentoOSRepository).save(captor.capture());
            assertThat(captor.getValue().getValorTotal()).isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("deve lançar exceção quando OS não encontrada")
        void deveLancarExcecaoSeOsNaoEncontrada() {
            when(ordemServicoRepository.buscarCompleta(osId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.registrar(osId, inputPagamento()))
                    .isInstanceOf(OrdemServicoException.class)
                    .hasMessageContaining("não encontrada");
        }

        @Test
        @DisplayName("deve lançar exceção quando OS não tem solução registrada")
        void deveLancarExcecaoSeSemSolucao() {
            os.setSolucao(null);
            when(ordemServicoRepository.buscarCompleta(osId)).thenReturn(Optional.of(os));

            assertThatThrownBy(() -> service.registrar(osId, inputPagamento()))
                    .isInstanceOf(OrdemServicoException.class)
                    .hasMessageContaining("solução");
        }

        @Test
        @DisplayName("deve lançar exceção quando pagamento já foi pago (pago=true)")
        void deveLancarExcecaoSePagamentoPago() {
            PagamentoOS pagamentoPago = new PagamentoOS();
            pagamentoPago.setPago(true);

            when(ordemServicoRepository.buscarCompleta(osId)).thenReturn(Optional.of(os));
            when(pagamentoOSRepository.findByOrdemServicoId(osId)).thenReturn(Optional.of(pagamentoPago));

            assertThatThrownBy(() -> service.registrar(osId, inputPagamento()))
                    .isInstanceOf(OrdemServicoException.class)
                    .hasMessageContaining("já foi registrado");
        }

        @Test
        @DisplayName("deve criar novo PagamentoOS quando não existe rascunho")
        void deveCriarNovoPagamentoSeNaoExisteRascunho() {
            when(ordemServicoRepository.buscarCompleta(osId)).thenReturn(Optional.of(os));
            when(pagamentoOSRepository.findByOrdemServicoId(osId)).thenReturn(Optional.empty());
            when(pagamentoOSRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(pagamentoOSOutputAssembler.toModel(any())).thenReturn(new PagamentoOSOutput());

            service.registrar(osId, inputPagamento());

            verify(pagamentoOSRepository).save(argThat(p -> p.isPago()));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // editar()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("editar()")
    class Editar {

        @Test
        @DisplayName("deve editar pagamento existente mantendo pago=true")
        void deveEditarPagamento() {
            PagamentoOS existente = new PagamentoOS();
            existente.setPago(true);

            when(ordemServicoRepository.buscarCompleta(osId)).thenReturn(Optional.of(os));
            when(pagamentoOSRepository.findByOrdemServicoId(osId)).thenReturn(Optional.of(existente));
            when(pagamentoOSRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(pagamentoOSOutputAssembler.toModel(any())).thenReturn(new PagamentoOSOutput());

            service.editar(osId, inputPagamento());

            ArgumentCaptor<PagamentoOS> captor = ArgumentCaptor.forClass(PagamentoOS.class);
            verify(pagamentoOSRepository).save(captor.capture());
            assertThat(captor.getValue().isPago()).isTrue();
        }

        @Test
        @DisplayName("deve atualizar campo pago quando explicitamente informado no input")
        void deveAtualizarCampoPago() {
            PagamentoOS existente = new PagamentoOS();
            existente.setPago(true);

            PagamentoOSInput input = inputPagamento();
            input.setPago(false);

            when(ordemServicoRepository.buscarCompleta(osId)).thenReturn(Optional.of(os));
            when(pagamentoOSRepository.findByOrdemServicoId(osId)).thenReturn(Optional.of(existente));
            when(pagamentoOSRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(pagamentoOSOutputAssembler.toModel(any())).thenReturn(new PagamentoOSOutput());

            service.editar(osId, input);

            ArgumentCaptor<PagamentoOS> captor = ArgumentCaptor.forClass(PagamentoOS.class);
            verify(pagamentoOSRepository).save(captor.capture());
            assertThat(captor.getValue().isPago()).isFalse();
        }

        @Test
        @DisplayName("deve lançar exceção quando nenhum pagamento existe para edição")
        void deveLancarExcecaoSeSemPagamento() {
            when(ordemServicoRepository.buscarCompleta(osId)).thenReturn(Optional.of(os));
            when(pagamentoOSRepository.findByOrdemServicoId(osId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.editar(osId, inputPagamento()))
                    .isInstanceOf(OrdemServicoException.class)
                    .hasMessageContaining("Registre o pagamento primeiro");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // buscarPorOrdemServico()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("buscarPorOrdemServico()")
    class BuscarPorOrdemServico {

        @Test
        @DisplayName("deve retornar output quando pagamento encontrado")
        void deveRetornarOutput() {
            PagamentoOS pagamento = new PagamentoOS();
            PagamentoOSOutput output = new PagamentoOSOutput();

            when(pagamentoOSRepository.findByOrdemServicoId(osId)).thenReturn(Optional.of(pagamento));
            when(pagamentoOSOutputAssembler.toModel(pagamento)).thenReturn(output);

            PagamentoOSOutput resultado = service.buscarPorOrdemServico(osId);

            assertThat(resultado).isEqualTo(output);
        }

        @Test
        @DisplayName("deve lançar exceção quando pagamento não encontrado")
        void deveLancarExcecaoSeNaoEncontrado() {
            when(pagamentoOSRepository.findByOrdemServicoId(osId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPorOrdemServico(osId))
                    .isInstanceOf(OrdemServicoException.class)
                    .hasMessageContaining("Pagamento não encontrado");
        }

        @Test
        @DisplayName("buscarPorOrdemServicoOpcional() deve retornar Optional vazio se não existe")
        void deveRetornarOptionalVazioSeNaoExiste() {
            when(pagamentoOSRepository.findByOrdemServicoId(osId)).thenReturn(Optional.empty());

            Optional<PagamentoOSOutput> result = service.buscarPorOrdemServicoOpcional(osId);

            assertThat(result).isEmpty();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────
    private PagamentoOSInput inputPagamento() {
        PagamentoOSInput input = new PagamentoOSInput();
        input.setValorChamado(new BigDecimal("100.00"));
        input.setValorKm(new BigDecimal("2.50"));
        input.setKm(new BigDecimal("0"));
        input.setPedagio(BigDecimal.ZERO);
        input.setEstacionamento(BigDecimal.ZERO);
        input.setValorOutros(BigDecimal.ZERO);
        input.setTipoPagamento(TipoPagamento.PIX);
        input.setDataPagamento(OffsetDateTime.now());
        return input;
    }
}
