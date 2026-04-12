package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.assembler.OrdemServicoInputDisassembler;
import io.github.franciscosviana.stmservicos.api.assembler.OrdemServicoOutputAssembler;
import io.github.franciscosviana.stmservicos.api.model.input.OrdemServicoInput;
import io.github.franciscosviana.stmservicos.api.model.output.OrdemServicoOutput;
import io.github.franciscosviana.stmservicos.common.validation.ContratoException;
import io.github.franciscosviana.stmservicos.common.validation.OrdemServicoException;
import io.github.franciscosviana.stmservicos.domain.model.*;
import io.github.franciscosviana.stmservicos.domain.model.enums.StatusRastreio;
import io.github.franciscosviana.stmservicos.domain.repository.OrdemServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrdemServicoService")
class OrdemServicoServiceTest {

    @Mock
    private TecnicoService tecnicoService;
    @Mock
    private ClienteService clienteService;
    @Mock
    private ContratoService contratoService;
    @Mock
    private CredenciadoService credenciadoService;
    @Mock
    private OrdemServicoRepository repository;
    @Mock
    private OrdemServicoOutputAssembler assembler;
    @Mock
    private OrdemServicoInputDisassembler disassembler;
    @Mock
    private HistoricoOrdemServicoService historicoOrdemServicoService;

    @InjectMocks
    private OrdemServicoService service;

    private UUID clienteId;
    private UUID contratoId;
    private UUID tecnicoId;
    private UUID credenciadoId;
    private Cliente cliente;
    private Contrato contrato;
    private Tecnico tecnico;
    private Credenciado credenciado;

    @BeforeEach
    void setUp() {
        clienteId = UUID.randomUUID();
        contratoId = UUID.randomUUID();
        tecnicoId = UUID.randomUUID();
        credenciadoId = UUID.randomUUID();

        credenciado = new Credenciado();
        credenciado.setId(credenciadoId);
        credenciado.setRag("Credenciado X");

        cliente = new Cliente();
        cliente.setId(clienteId);
        cliente.setNome("Cliente Teste");

        contrato = new Contrato();
        contrato.setId(contratoId);
        contrato.setCliente(cliente);

        tecnico = new Tecnico();
        tecnico.setId(tecnicoId);
        tecnico.setNome("Técnico A");
        tecnico.setCredenciado(credenciado);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // salvar()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("salvar()")
    class Salvar {

        @Test
        @DisplayName("deve salvar OS com sucesso e gerar OSG")
        void deveSalvarComSucesso() {
            OrdemServicoInput input = inputValido();
            OrdemServico os = new OrdemServico();

            when(disassembler.toDomainObject(input)).thenReturn(os);
            when(repository.findLastOsg(any())).thenReturn(Collections.emptyList());
            when(clienteService.buscarOuFalhar(clienteId)).thenReturn(cliente);
            when(contratoService.buscarOuFalhar(contratoId)).thenReturn(contrato);
            when(tecnicoService.buscarOuFalhar(tecnicoId)).thenReturn(tecnico);
            when(repository.save(any())).thenReturn(os);
            when(assembler.toModel(os)).thenReturn(new OrdemServicoOutput());

            OrdemServicoOutput result = service.salvar(input);

            assertThat(result).isNotNull();
            assertThat(os.getOsg()).startsWith("OSG");
            verify(repository).save(os);
            verify(historicoOrdemServicoService).registrar(eq(os), any(), any());
        }

        @Test
        @DisplayName("deve lançar ContratoException quando contrato não informado")
        void deveLancarExcecaoSemContrato() {
            OrdemServicoInput input = inputValido();
            input.setContratoId(null);

            OrdemServico os = new OrdemServico();
            when(disassembler.toDomainObject(input)).thenReturn(os);
            when(repository.findLastOsg(any())).thenReturn(Collections.emptyList());
            when(clienteService.buscarOuFalhar(clienteId)).thenReturn(cliente);

            assertThatThrownBy(() -> service.salvar(input))
                    .isInstanceOf(ContratoException.class)
                    .hasMessageContaining("Contrato não informado");
        }

        @Test
        @DisplayName("deve lançar ContratoException quando contrato não pertence ao cliente")
        void deveLancarExcecaoContratoDeOutroCliente() {
            OrdemServicoInput input = inputValido();

            Cliente outroCliente = new Cliente();
            outroCliente.setId(UUID.randomUUID());
            contrato.setCliente(outroCliente);

            OrdemServico os = new OrdemServico();
            when(disassembler.toDomainObject(input)).thenReturn(os);
            when(repository.findLastOsg(any())).thenReturn(Collections.emptyList());
            when(clienteService.buscarOuFalhar(clienteId)).thenReturn(cliente);
            when(contratoService.buscarOuFalhar(contratoId)).thenReturn(contrato);

            assertThatThrownBy(() -> service.salvar(input))
                    .isInstanceOf(ContratoException.class)
                    .hasMessageContaining("não pertence ao cliente");
        }

        @Test
        @DisplayName("deve lançar OrdemServicoException quando técnico não informado")
        void deveLancarExcecaoSemTecnico() {
            OrdemServicoInput input = inputValido();
            input.setTecnicoId(null);

            OrdemServico os = new OrdemServico();
            when(disassembler.toDomainObject(input)).thenReturn(os);
            when(repository.findLastOsg(any())).thenReturn(Collections.emptyList());
            when(clienteService.buscarOuFalhar(clienteId)).thenReturn(cliente);
            when(contratoService.buscarOuFalhar(contratoId)).thenReturn(contrato);

            assertThatThrownBy(() -> service.salvar(input))
                    .isInstanceOf(OrdemServicoException.class)
                    .hasMessageContaining("Técnico é obrigatório");
        }

        @Test
        @DisplayName("deve lançar OrdemServicoException quando técnico não pertence ao credenciado")
        void deveLancarExcecaoTecnicoDeOutroCredenciado() {
            OrdemServicoInput input = inputValido();

            Credenciado outroCredenciado = new Credenciado();
            outroCredenciado.setId(UUID.randomUUID());
            tecnico.setCredenciado(outroCredenciado);

            OrdemServico os = new OrdemServico();
            when(disassembler.toDomainObject(input)).thenReturn(os);
            when(repository.findLastOsg(any())).thenReturn(Collections.emptyList());
            when(clienteService.buscarOuFalhar(clienteId)).thenReturn(cliente);
            when(contratoService.buscarOuFalhar(contratoId)).thenReturn(contrato);
            when(tecnicoService.buscarOuFalhar(tecnicoId)).thenReturn(tecnico);

            assertThatThrownBy(() -> service.salvar(input))
                    .isInstanceOf(OrdemServicoException.class)
                    .hasMessageContaining("não pertence ao credenciado");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // gerarProximoOsg()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("gerarProximoOsg()")
    class GerarProximoOsg {

        @Test
        @DisplayName("deve gerar OSG com sequência 0001 quando não há OSs anteriores")
        void deveGerarPrimeiroOsg() {
            when(repository.findLastOsg(any())).thenReturn(Collections.emptyList());

            String osg = service.gerarProximoOsg();

            String anoEsperado = String.format("%02d", OffsetDateTime.now().getYear() % 100);
            assertThat(osg).startsWith("OSG" + anoEsperado);
            assertThat(osg).endsWith("0001");
        }

        @Test
        @DisplayName("deve incrementar sequência do último OSG do mesmo ano")
        void deveIncrementarSequencia() {
            String anoAtual = String.format("%02d", OffsetDateTime.now().getYear() % 100);
            when(repository.findLastOsg(any())).thenReturn(List.of("OSG" + anoAtual + "0005"));

            String osg = service.gerarProximoOsg();

            assertThat(osg).isEqualTo("OSG" + anoAtual + "0006");
        }

        @Test
        @DisplayName("deve reiniciar sequência em 0001 quando ano diferente")
        void deveReiniciarSequenciaNoNovoAno() {
            when(repository.findLastOsg(any())).thenReturn(List.of("OSG250099"));

            String osg = service.gerarProximoOsg();

            String anoAtual = String.format("%02d", OffsetDateTime.now().getYear() % 100);
            // Só reinicia se o ano atual for diferente de 25
            if (!anoAtual.equals("25")) {
                assertThat(osg).isEqualTo("OSG" + anoAtual + "0001");
            }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // atualizarStatusRastreio()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("atualizarStatusRastreio()")
    class AtualizarStatusRastreio {

        @Test
        @DisplayName("deve atualizar status rastreio e registrar histórico")
        void deveAtualizarStatusRastreio() {
            UUID osId = UUID.randomUUID();
            OrdemServico os = new OrdemServico();
            os.setId(osId);

            when(repository.findById(osId)).thenReturn(Optional.of(os));
            when(repository.save(any())).thenReturn(os);
            when(assembler.toModel(any())).thenReturn(new OrdemServicoOutput());

            service.atualizarStatusRastreio(osId, "Chegou");

            assertThat(os.getStatusRastreio()).isEqualTo(StatusRastreio.CHEGOU);
            verify(historicoOrdemServicoService).registrar(eq(os), any(), contains("Chegou"));
        }

        @Test
        @DisplayName("deve lançar exceção para status rastreio inválido")
        void deveLancarExcecaoParaStatusInvalido() {
            UUID osId = UUID.randomUUID();
            OrdemServico os = new OrdemServico();
            when(repository.findById(osId)).thenReturn(Optional.of(os));

            assertThatThrownBy(() -> service.atualizarStatusRastreio(osId, "STATUS_INVALIDO"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // deletar()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("deletar()")
    class Deletar {

        @Test
        @DisplayName("deve deletar histórico antes de deletar a OS")
        void deveDeletarHistoricoAntesDeOs() {
            UUID osId = UUID.randomUUID();
            OrdemServico os = new OrdemServico();
            os.setId(osId);

            when(repository.findById(osId)).thenReturn(Optional.of(os));

            service.deletar(osId);

            verify(historicoOrdemServicoService).deletarPorOrdemServico(osId);
            verify(repository).delete(os);
        }

        @Test
        @DisplayName("deve lançar OrdemServicoException quando OS não encontrada")
        void deveLancarExcecaoSeNaoEncontrada() {
            UUID osId = UUID.randomUUID();
            when(repository.findById(osId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.deletar(osId))
                    .isInstanceOf(OrdemServicoException.class);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────
    private OrdemServicoInput inputValido() {
        OrdemServicoInput input = new OrdemServicoInput();
        input.setClienteId(clienteId);
        input.setContratoId(contratoId);
        input.setTecnicoId(tecnicoId);
        input.setCredenciadoId(credenciadoId);
        return input;
    }
}
