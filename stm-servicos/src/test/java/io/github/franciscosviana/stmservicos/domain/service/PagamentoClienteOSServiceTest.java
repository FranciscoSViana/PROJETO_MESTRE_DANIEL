package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.assembler.PagamentoClienteOSOutputAssembler;
import io.github.franciscosviana.stmservicos.api.model.input.PagamentoClienteOSInput;
import io.github.franciscosviana.stmservicos.api.model.input.PagamentoLoteInput;
import io.github.franciscosviana.stmservicos.api.model.output.PagamentoClienteOSOutput;
import io.github.franciscosviana.stmservicos.api.model.output.PagamentoLoteResultado;
import io.github.franciscosviana.stmservicos.common.validation.OrdemServicoException;
import io.github.franciscosviana.stmservicos.domain.model.*;
import io.github.franciscosviana.stmservicos.domain.repository.OrdemServicoRepository;
import io.github.franciscosviana.stmservicos.domain.repository.PagamentoClienteOSRepository;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("PagamentoClienteOSService")
@ExtendWith(MockitoExtension.class)
class PagamentoClienteOSServiceTest {

    @Mock
    PagamentoClienteOSRepository pagamentoClienteOSRepository;

    @Mock
    OrdemServicoRepository ordemServicoRepository;

    @Mock
    PagamentoClienteOSOutputAssembler assembler;

    @InjectMocks
    PagamentoClienteOSService service;

    private UUID osId;
    private OrdemServico os;
    private SolucaoOS solucao;

    @BeforeEach
    void setUp() {
        osId = UUID.randomUUID();

        Cliente cliente = new Cliente();
        cliente.setNome("EMPRESA X");
        cliente.setValorChamado(new BigDecimal("150.00"));
        cliente.setValorKm(new BigDecimal("2.50"));

        Contrato contrato = new Contrato();
        contrato.setId(UUID.randomUUID());

        Credenciado credenciado = new Credenciado();
        credenciado.setValorChamado(new BigDecimal("120.00"));
        credenciado.setValorKm(new BigDecimal("2.00"));

        os = new OrdemServico();
        os.setId(osId);
        os.setOsg("OSG260001");
        os.setOsClt("OS-CLI-001");
        os.setCliente(cliente);
        os.setContrato(contrato);
        os.setCredenciado(credenciado);

        solucao = new SolucaoOS();
        solucao.setKm(new BigDecimal("10"));
        solucao.setPedagio(BigDecimal.ZERO);
        solucao.setEstacionamento(BigDecimal.ZERO);
        solucao.setOutros(BigDecimal.ZERO);
        os.setSolucao(solucao);
    }

    @Nested
    @DisplayName("registrar()")
    class Registrar {

        @Test
        @DisplayName("deve criar pagamento com valores do input")
        void deveCriarPagamento() {
            PagamentoClienteOSInput input = inputPadrao();

            when(ordemServicoRepository.buscarCompleta(osId)).thenReturn(Optional.of(os));
            when(pagamentoClienteOSRepository.findByOrdemServicoId(osId)).thenReturn(Optional.empty());
            when(pagamentoClienteOSRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(assembler.toModel(any())).thenReturn(new PagamentoClienteOSOutput());

            PagamentoClienteOSOutput result = service.registrar(osId, input);

            assertThat(result).isNotNull();
            ArgumentCaptor<PagamentoClienteOS> captor =
                    ArgumentCaptor.forClass(PagamentoClienteOS.class);
            verify(pagamentoClienteOSRepository).save(captor.capture());
            assertThat(captor.getValue().isCorrigido()).isTrue();
        }

        @Test
        @DisplayName("deve lançar OrdemServicoException quando OS não encontrada")
        void deveLancarExcecaoSeOsNaoEncontrada() {
            when(ordemServicoRepository.buscarCompleta(osId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.registrar(osId, inputPadrao()))
                    .isInstanceOf(OrdemServicoException.class)
                    .hasMessageContaining("não encontrada");
        }

        @Test
        @DisplayName("deve lançar OrdemServicoException quando OS não tem solução")
        void deveLancarExcecaoSeOsSemSolucao() {
            os.setSolucao(null);
            when(ordemServicoRepository.buscarCompleta(osId)).thenReturn(Optional.of(os));

            assertThatThrownBy(() -> service.registrar(osId, inputPadrao()))
                    .isInstanceOf(OrdemServicoException.class)
                    .hasMessageContaining("solução");
        }

        @Test
        @DisplayName("deve usar valorChamado do cliente quando input não informa")
        void deveUsarValorChamadoDoCliente() {
            PagamentoClienteOSInput input = inputPadrao();
            input.setValorChamado(null);

            when(ordemServicoRepository.buscarCompleta(osId)).thenReturn(Optional.of(os));
            when(pagamentoClienteOSRepository.findByOrdemServicoId(osId)).thenReturn(Optional.empty());
            when(pagamentoClienteOSRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(assembler.toModel(any())).thenReturn(new PagamentoClienteOSOutput());

            service.registrar(osId, input);

            ArgumentCaptor<PagamentoClienteOS> captor =
                    ArgumentCaptor.forClass(PagamentoClienteOS.class);
            verify(pagamentoClienteOSRepository).save(captor.capture());
            assertThat(captor.getValue().getValorChamado())
                    .isEqualByComparingTo("150.00");
        }
    }

    @Nested
    @DisplayName("editar()")
    class Editar {

        @Test
        @DisplayName("deve editar pagamento existente")
        void deveEditarPagamento() {
            PagamentoClienteOS existente = new PagamentoClienteOS();

            when(ordemServicoRepository.buscarCompleta(osId)).thenReturn(Optional.of(os));
            when(pagamentoClienteOSRepository.findByOrdemServicoId(osId))
                    .thenReturn(Optional.of(existente));
            when(pagamentoClienteOSRepository.save(any())).thenReturn(existente);
            when(assembler.toModel(any())).thenReturn(new PagamentoClienteOSOutput());

            PagamentoClienteOSOutput result = service.editar(osId, inputPadrao());

            assertThat(result).isNotNull();
            verify(pagamentoClienteOSRepository).save(existente);
        }

        @Test
        @DisplayName("deve lançar OrdemServicoException quando pagamento não encontrado")
        void deveLancarExcecaoSePagamentoNaoEncontrado() {
            when(ordemServicoRepository.buscarCompleta(osId)).thenReturn(Optional.of(os));
            when(pagamentoClienteOSRepository.findByOrdemServicoId(osId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.editar(osId, inputPadrao()))
                    .isInstanceOf(OrdemServicoException.class)
                    .hasMessageContaining("Registre o recebimento primeiro");
        }
    }

    @Nested
    @DisplayName("buscarPorOrdemServico()")
    class BuscarPorOrdemServico {

        @Test
        @DisplayName("deve retornar pagamento quando encontrado")
        void deveRetornarPagamento() {
            PagamentoClienteOS pagamento = new PagamentoClienteOS();
            PagamentoClienteOSOutput output = new PagamentoClienteOSOutput();

            when(pagamentoClienteOSRepository.findByOrdemServicoId(osId))
                    .thenReturn(Optional.of(pagamento));
            when(assembler.toModel(pagamento)).thenReturn(output);

            PagamentoClienteOSOutput result = service.buscarPorOrdemServico(osId);

            assertThat(result).isEqualTo(output);
        }

        @Test
        @DisplayName("deve lançar OrdemServicoException quando pagamento não encontrado")
        void deveLancarExcecaoSeNaoEncontrado() {
            when(pagamentoClienteOSRepository.findByOrdemServicoId(osId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPorOrdemServico(osId))
                    .isInstanceOf(OrdemServicoException.class);
        }
    }

    @Nested
    @DisplayName("buscarPorOrdemServicoOpcional()")
    class BuscarPorOrdemServicoOpcional {

        @Test
        @DisplayName("deve retornar Optional com pagamento quando encontrado")
        void deveRetornarOptionalPreenchido() {
            PagamentoClienteOS pagamento = new PagamentoClienteOS();
            PagamentoClienteOSOutput output = new PagamentoClienteOSOutput();

            when(pagamentoClienteOSRepository.findByOrdemServicoId(osId))
                    .thenReturn(Optional.of(pagamento));
            when(assembler.toModel(pagamento)).thenReturn(output);

            Optional<PagamentoClienteOSOutput> result =
                    service.buscarPorOrdemServicoOpcional(osId);

            assertThat(result).isPresent().contains(output);
        }

        @Test
        @DisplayName("deve retornar Optional vazio quando não encontrado")
        void deveRetornarOptionalVazio() {
            when(pagamentoClienteOSRepository.findByOrdemServicoId(osId))
                    .thenReturn(Optional.empty());

            Optional<PagamentoClienteOSOutput> result =
                    service.buscarPorOrdemServicoOpcional(osId);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("registrarLote()")
    class RegistrarLote {

        @Test
        @DisplayName("deve processar lote com sucesso para OS com solução")
        void deveProcessarLoteComSucesso() {
            PagamentoLoteInput loteInput = new PagamentoLoteInput();
            loteInput.setOrdemServicoIds(List.of(osId));
            loteInput.setLote("LOTE-01");
            loteInput.setDataPagamento(LocalDate.now());

            when(ordemServicoRepository.buscarCompleta(osId)).thenReturn(Optional.of(os));
            when(pagamentoClienteOSRepository.findByOrdemServicoId(osId))
                    .thenReturn(Optional.empty());
            when(pagamentoClienteOSRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            PagamentoLoteResultado resultado = service.registrarLote(loteInput);

            assertThat(resultado.getTotalSucesso()).isEqualTo(1);
            assertThat(resultado.getTotalErro()).isZero();
        }

        @Test
        @DisplayName("deve registrar erro para OS não encontrada no lote")
        void deveRegistrarErroParaOsNaoEncontrada() {
            UUID idInexistente = UUID.randomUUID();
            PagamentoLoteInput loteInput = new PagamentoLoteInput();
            loteInput.setOrdemServicoIds(List.of(idInexistente));
            loteInput.setDataPagamento(LocalDate.now());

            when(ordemServicoRepository.buscarCompleta(idInexistente)).thenReturn(Optional.empty());

            PagamentoLoteResultado resultado = service.registrarLote(loteInput);

            assertThat(resultado.getTotalErro()).isEqualTo(1);
            assertThat(resultado.getTotalSucesso()).isZero();
            assertThat(resultado.getErros()).isNotEmpty();
        }

        @Test
        @DisplayName("deve registrar erro para OS já paga")
        void deveRegistrarErroParaOsJaPaga() {
            PagamentoClienteOS pagamentoExistente = new PagamentoClienteOS();
            pagamentoExistente.setPago(true);

            PagamentoLoteInput loteInput = new PagamentoLoteInput();
            loteInput.setOrdemServicoIds(List.of(osId));
            loteInput.setDataPagamento(LocalDate.now());

            when(ordemServicoRepository.buscarCompleta(osId)).thenReturn(Optional.of(os));
            when(pagamentoClienteOSRepository.findByOrdemServicoId(osId))
                    .thenReturn(Optional.of(pagamentoExistente));

            PagamentoLoteResultado resultado = service.registrarLote(loteInput);

            assertThat(resultado.getTotalErro()).isEqualTo(1);
            assertThat(resultado.getErros().get(0)).contains("já foi paga");
        }
    }

    private PagamentoClienteOSInput inputPadrao() {
        PagamentoClienteOSInput input = new PagamentoClienteOSInput();
        input.setValorChamado(new BigDecimal("150.00"));
        input.setKm(new BigDecimal("10"));
        input.setValorKm(new BigDecimal("2.50"));
        input.setPedagio(BigDecimal.ZERO);
        input.setEstacionamento(BigDecimal.ZERO);
        input.setValorOutros(BigDecimal.ZERO);
        input.setDataPagamento(LocalDate.now());
        return input;
    }
}
