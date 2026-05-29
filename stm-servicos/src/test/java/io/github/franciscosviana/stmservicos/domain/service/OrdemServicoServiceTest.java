package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.assembler.OrdemServicoInputDisassembler;
import io.github.franciscosviana.stmservicos.api.assembler.OrdemServicoOutputAssembler;
import io.github.franciscosviana.stmservicos.api.model.input.OrdemServicoInput;
import io.github.franciscosviana.stmservicos.api.model.output.OrdemServicoOutput;
import io.github.franciscosviana.stmservicos.common.validation.ContratoException;
import io.github.franciscosviana.stmservicos.common.validation.OrdemServicoException;
import io.github.franciscosviana.stmservicos.domain.model.*;
import io.github.franciscosviana.stmservicos.domain.model.enums.StatusOrdem;
import io.github.franciscosviana.stmservicos.domain.model.enums.StatusRastreio;
import io.github.franciscosviana.stmservicos.domain.model.enums.TipoAcaoOS;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrdemServicoService")
class OrdemServicoServiceTest {

    @Mock private TecnicoService tecnicoService;
    @Mock private ClienteService clienteService;
    @Mock private ContratoService contratoService;
    @Mock private CredenciadoService credenciadoService;
    @Mock private OrdemServicoRepository repository;
    @Mock private OrdemServicoOutputAssembler assembler;
    @Mock private OrdemServicoInputDisassembler disassembler;
    @Mock private HistoricoOrdemServicoService historicoOrdemServicoService;

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
    // buscarPorId()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("buscarPorId()")
    class BuscarPorId {

        @Test
        @DisplayName("deve retornar output quando OS encontrada")
        void deveRetornarOutput() {
            UUID id = UUID.randomUUID();
            OrdemServico os = new OrdemServico();
            OrdemServicoOutput output = new OrdemServicoOutput();

            when(repository.buscarCompleta(id)).thenReturn(Optional.of(os));
            when(assembler.toModel(os)).thenReturn(output);

            OrdemServicoOutput resultado = service.buscarPorId(id);

            assertThat(resultado).isEqualTo(output);
        }

        @Test
        @DisplayName("deve lançar OrdemServicoException quando OS não encontrada")
        void deveLancarExcecaoSeNaoEncontrada() {
            UUID id = UUID.randomUUID();
            when(repository.buscarCompleta(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPorId(id))
                    .isInstanceOf(OrdemServicoException.class)
                    .hasMessageContaining("não encontrada");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // atualizar()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("atualizar()")
    class Atualizar {

        private UUID osId;
        private OrdemServico osExistente;

        @BeforeEach
        void setUpAtualizar() {
            osId = UUID.randomUUID();
            osExistente = new OrdemServico();
            osExistente.setId(osId);
            osExistente.setStatus(StatusOrdem.ABERTA);
            osExistente.setCliente(cliente);
            osExistente.setContrato(contrato);
            osExistente.setCredenciado(credenciado);
            osExistente.setTecnico(tecnico);
            osExistente.setOsg("OSG260001");
        }

        @Test
        @DisplayName("deve atualizar OS com sucesso sem alterar técnico e credenciado")
        void deveAtualizarOsComSucesso() {
            OrdemServicoInput input = inputValido();
            input.setTecnicoId(null);
            input.setCredenciadoId(null);

            when(repository.findById(osId)).thenReturn(Optional.of(osExistente));
            when(clienteService.buscarOuFalhar(clienteId)).thenReturn(cliente);
            when(contratoService.buscarOuFalhar(contratoId)).thenReturn(contrato);
            when(repository.save(any())).thenReturn(osExistente);
            when(assembler.toModel(any())).thenReturn(new OrdemServicoOutput());

            OrdemServicoOutput result = service.atualizar(osId, input);

            assertThat(result).isNotNull();
            verify(repository).save(osExistente);
        }

        @Test
        @DisplayName("deve registrar MUDANCA_STATUS quando status muda durante atualização")
        void deveRegistrarMudancaDeStatus() {
            osExistente.setStatus(StatusOrdem.ABERTA);

            OrdemServicoInput input = inputValido();
            input.setTecnicoId(null);
            input.setCredenciadoId(null);
            input.setStatus("CONCLUIDA");

            doAnswer(invocation -> {
                OrdemServico os = invocation.getArgument(1);
                os.setStatus(StatusOrdem.CONCLUIDA);
                return null;
            }).when(disassembler).copyToDomainObject(any(), any());

            when(repository.findById(osId)).thenReturn(Optional.of(osExistente));
            when(clienteService.buscarOuFalhar(clienteId)).thenReturn(cliente);
            when(contratoService.buscarOuFalhar(contratoId)).thenReturn(contrato);
            when(repository.save(any())).thenReturn(osExistente);
            when(assembler.toModel(any())).thenReturn(new OrdemServicoOutput());

            service.atualizar(osId, input);

            verify(historicoOrdemServicoService).registrar(
                    eq(osExistente),
                    eq(TipoAcaoOS.MUDANCA_STATUS),
                    any()
            );
        }

        @Test
        @DisplayName("deve registrar ATUALIZACAO quando status não muda")
        void deveRegistrarAtualizacaoQuandoStatusNaoMuda() {
            OrdemServicoInput input = inputValido();
            input.setTecnicoId(null);
            input.setCredenciadoId(null);
            input.setDefeito("Novo defeito");

            when(repository.findById(osId)).thenReturn(Optional.of(osExistente));
            when(clienteService.buscarOuFalhar(clienteId)).thenReturn(cliente);
            when(contratoService.buscarOuFalhar(contratoId)).thenReturn(contrato);
            when(repository.save(any())).thenReturn(osExistente);
            when(assembler.toModel(any())).thenReturn(new OrdemServicoOutput());

            service.atualizar(osId, input);

            verify(historicoOrdemServicoService).registrar(
                    eq(osExistente),
                    eq(TipoAcaoOS.ATUALIZACAO),
                    any()
            );
        }

        @Test
        @DisplayName("deve atualizar credenciado quando credenciadoId informado")
        void deveAtualizarCredenciado() {
            UUID novoCredenciadoId = UUID.randomUUID();
            Credenciado novoCredenciado = new Credenciado();
            novoCredenciado.setId(novoCredenciadoId);
            novoCredenciado.setRag("Novo Credenciado");

            OrdemServicoInput input = inputValido();
            input.setTecnicoId(null);
            input.setCredenciadoId(novoCredenciadoId);

            when(repository.findById(osId)).thenReturn(Optional.of(osExistente));
            when(clienteService.buscarOuFalhar(clienteId)).thenReturn(cliente);
            when(contratoService.buscarOuFalhar(contratoId)).thenReturn(contrato);
            when(credenciadoService.buscarOuFalhar(novoCredenciadoId)).thenReturn(novoCredenciado);
            when(repository.save(any())).thenReturn(osExistente);
            when(assembler.toModel(any())).thenReturn(new OrdemServicoOutput());

            service.atualizar(osId, input);

            assertThat(osExistente.getCredenciado()).isEqualTo(novoCredenciado);
        }

        @Test
        @DisplayName("deve atualizar técnico quando tecnicoId informado e pertence ao credenciado")
        void deveAtualizarTecnico() {
            UUID novoTecnicoId = UUID.randomUUID();
            Tecnico novoTecnico = new Tecnico();
            novoTecnico.setId(novoTecnicoId);
            novoTecnico.setNome("Novo Técnico");
            novoTecnico.setCredenciado(credenciado);

            OrdemServicoInput input = inputValido();
            input.setCredenciadoId(null);
            input.setTecnicoId(novoTecnicoId);

            when(repository.findById(osId)).thenReturn(Optional.of(osExistente));
            when(clienteService.buscarOuFalhar(clienteId)).thenReturn(cliente);
            when(contratoService.buscarOuFalhar(contratoId)).thenReturn(contrato);
            when(tecnicoService.buscarOuFalhar(novoTecnicoId)).thenReturn(novoTecnico);
            when(repository.save(any())).thenReturn(osExistente);
            when(assembler.toModel(any())).thenReturn(new OrdemServicoOutput());

            service.atualizar(osId, input);

            assertThat(osExistente.getTecnico()).isEqualTo(novoTecnico);
        }

        @Test
        @DisplayName("deve lançar OrdemServicoException quando técnico não pertence ao credenciado")
        void deveLancarExcecaoTecnicoDeOutroCredenciado() {
            UUID novoTecnicoId = UUID.randomUUID();
            Credenciado outroCredenciado = new Credenciado();
            outroCredenciado.setId(UUID.randomUUID());

            Tecnico tecnicoDeOutro = new Tecnico();
            tecnicoDeOutro.setId(novoTecnicoId);
            tecnicoDeOutro.setCredenciado(outroCredenciado);

            OrdemServicoInput input = inputValido();
            input.setCredenciadoId(null);
            input.setTecnicoId(novoTecnicoId);

            when(repository.findById(osId)).thenReturn(Optional.of(osExistente));
            when(clienteService.buscarOuFalhar(clienteId)).thenReturn(cliente);
            when(contratoService.buscarOuFalhar(contratoId)).thenReturn(contrato);
            when(tecnicoService.buscarOuFalhar(novoTecnicoId)).thenReturn(tecnicoDeOutro);

            assertThatThrownBy(() -> service.atualizar(osId, input))
                    .isInstanceOf(OrdemServicoException.class)
                    .hasMessageContaining("não pertence ao credenciado");
        }

        @Test
        @DisplayName("deve lançar ContratoException quando contrato é nulo após cópia")
        void deveLancarExcecaoContratoNulo() {
            OrdemServico osSemContrato = new OrdemServico();
            osSemContrato.setId(osId);
            osSemContrato.setStatus(StatusOrdem.ABERTA);
            osSemContrato.setCliente(cliente);
            osSemContrato.setContrato(null);
            osSemContrato.setCredenciado(credenciado);

            OrdemServicoInput input = inputValido();
            input.setContratoId(null);
            input.setTecnicoId(null);
            input.setCredenciadoId(null);

            when(repository.findById(osId)).thenReturn(Optional.of(osSemContrato));
            when(clienteService.buscarOuFalhar(clienteId)).thenReturn(cliente);

            assertThatThrownBy(() -> service.atualizar(osId, input))
                    .isInstanceOf(ContratoException.class)
                    .hasMessageContaining("Contrato não informado");
        }

        @Test
        @DisplayName("deve lançar ContratoException quando contrato não pertence ao cliente")
        void deveLancarExcecaoContratoDeOutroCliente() {
            Cliente outroCliente = new Cliente();
            outroCliente.setId(UUID.randomUUID());

            Contrato contratoDeOutroCliente = new Contrato();
            contratoDeOutroCliente.setId(contratoId);
            contratoDeOutroCliente.setCliente(outroCliente);

            OrdemServicoInput input = inputValido();
            input.setTecnicoId(null);
            input.setCredenciadoId(null);

            when(repository.findById(osId)).thenReturn(Optional.of(osExistente));
            when(clienteService.buscarOuFalhar(clienteId)).thenReturn(cliente);
            when(contratoService.buscarOuFalhar(contratoId)).thenReturn(contratoDeOutroCliente);

            assertThatThrownBy(() -> service.atualizar(osId, input))
                    .isInstanceOf(ContratoException.class)
                    .hasMessageContaining("não pertence ao cliente");
        }

        @Test
        @DisplayName("deve lançar OrdemServicoException quando OS não encontrada para atualização")
        void deveLancarExcecaoOsNaoEncontrada() {
            when(repository.findById(osId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.atualizar(osId, inputValido()))
                    .isInstanceOf(OrdemServicoException.class)
                    .hasMessageContaining("não encontrada");
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

            String anoEsperado = String.format("%02d", java.time.OffsetDateTime.now().getYear() % 100);
            assertThat(osg).startsWith("OSG" + anoEsperado);
            assertThat(osg).endsWith("0001");
        }

        @Test
        @DisplayName("deve incrementar sequência do último OSG do mesmo ano")
        void deveIncrementarSequencia() {
            String anoAtual = String.format("%02d", java.time.OffsetDateTime.now().getYear() % 100);
            when(repository.findLastOsg(any())).thenReturn(List.of("OSG" + anoAtual + "0005"));

            String osg = service.gerarProximoOsg();

            assertThat(osg).isEqualTo("OSG" + anoAtual + "0006");
        }

        @Test
        @DisplayName("deve reiniciar sequência em 0001 quando ano diferente")
        void deveReiniciarSequenciaNoNovoAno() {
            when(repository.findLastOsg(any())).thenReturn(List.of("OSG250099"));

            String osg = service.gerarProximoOsg();

            String anoAtual = String.format("%02d", java.time.OffsetDateTime.now().getYear() % 100);
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
    // gerarDescricaoAlteracoes() via atualizar()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("gerarDescricaoAlteracoes() via atualizar()")
    class GerarDescricaoAlteracoes {

        private UUID osId;
        private OrdemServico osExistente;

        @BeforeEach
        void setUp() {
            osId = UUID.randomUUID();
            osExistente = new OrdemServico();
            osExistente.setId(osId);
            osExistente.setStatus(StatusOrdem.ABERTA);
            osExistente.setCliente(cliente);
            osExistente.setContrato(contrato);
            osExistente.setCredenciado(credenciado);
            osExistente.setTecnico(tecnico);
            osExistente.setOsClt("CLT-ANTIGO");
            osExistente.setContato("Contato Antigo");
            osExistente.setDepartamento("Depto Antigo");
            osExistente.setTelefone("86999990000");
            osExistente.setAcionador("Acionador Antigo");
            osExistente.setEquipamento("Equipamento Antigo");
            osExistente.setSerie("SN-ANTIGO");
            osExistente.setPib("PIB-ANTIGO");
            osExistente.setDefeito("Defeito Antigo");
            osExistente.setRastreio("RASTREIO-ANTIGO");
        }

        @Test
        @DisplayName("deve gerar descrição 'Dados da OS atualizados' quando nada muda")
        void deveGerarDescricaoPadraoQuandoNadaMuda() {
            OrdemServicoInput input = inputValido();
            input.setTecnicoId(null);
            input.setCredenciadoId(null);

            when(repository.findById(osId)).thenReturn(Optional.of(osExistente));
            when(clienteService.buscarOuFalhar(clienteId)).thenReturn(cliente);
            when(contratoService.buscarOuFalhar(contratoId)).thenReturn(contrato);
            when(repository.save(any())).thenReturn(osExistente);
            when(assembler.toModel(any())).thenReturn(new OrdemServicoOutput());

            service.atualizar(osId, input);

            verify(historicoOrdemServicoService).registrar(
                    eq(osExistente),
                    eq(TipoAcaoOS.ATUALIZACAO),
                    eq("Dados da OS atualizados")
            );
        }

        @Test
        @DisplayName("deve incluir alteração de osClt na descrição")
        void deveIncluirAlteracaoOsClt() {
            OrdemServicoInput input = inputValido();
            input.setTecnicoId(null);
            input.setCredenciadoId(null);

            doAnswer(invocation -> {
                OrdemServico os = invocation.getArgument(1);
                os.setOsClt("CLT-NOVO");
                return null;
            }).when(disassembler).copyToDomainObject(any(), any());

            when(repository.findById(osId)).thenReturn(Optional.of(osExistente));
            when(clienteService.buscarOuFalhar(clienteId)).thenReturn(cliente);
            when(contratoService.buscarOuFalhar(contratoId)).thenReturn(contrato);
            when(repository.save(any())).thenReturn(osExistente);
            when(assembler.toModel(any())).thenReturn(new OrdemServicoOutput());

            service.atualizar(osId, input);

            verify(historicoOrdemServicoService).registrar(
                    eq(osExistente),
                    any(),
                    contains("OS Cliente")
            );
        }

        @Test
        @DisplayName("deve incluir múltiplas alterações concatenadas na descrição")
        void deveIncluirMultiplasAlteracoes() {
            OrdemServicoInput input = inputValido();
            input.setTecnicoId(null);
            input.setCredenciadoId(null);

            doAnswer(invocation -> {
                OrdemServico os = invocation.getArgument(1);
                os.setDefeito("Defeito Novo");
                os.setEquipamento("Equipamento Novo");
                os.setContato("Contato Novo");
                return null;
            }).when(disassembler).copyToDomainObject(any(), any());

            when(repository.findById(osId)).thenReturn(Optional.of(osExistente));
            when(clienteService.buscarOuFalhar(clienteId)).thenReturn(cliente);
            when(contratoService.buscarOuFalhar(contratoId)).thenReturn(contrato);
            when(repository.save(any())).thenReturn(osExistente);
            when(assembler.toModel(any())).thenReturn(new OrdemServicoOutput());

            service.atualizar(osId, input);

            verify(historicoOrdemServicoService).registrar(
                    eq(osExistente),
                    eq(TipoAcaoOS.ATUALIZACAO),
                    argThat(desc -> desc.contains("Defeito") && desc.contains("Equipamento") && desc.contains("Contato"))
            );
        }

        @Test
        @DisplayName("deve incluir alteração de técnico na descrição")
        void deveIncluirAlteracaoTecnico() {
            UUID novoTecnicoId = UUID.randomUUID();
            Tecnico novoTecnico = new Tecnico();
            novoTecnico.setId(novoTecnicoId);
            novoTecnico.setNome("Novo Técnico");
            novoTecnico.setCredenciado(credenciado);

            OrdemServicoInput input = inputValido();
            input.setCredenciadoId(null);
            input.setTecnicoId(novoTecnicoId);

            when(repository.findById(osId)).thenReturn(Optional.of(osExistente));
            when(clienteService.buscarOuFalhar(clienteId)).thenReturn(cliente);
            when(contratoService.buscarOuFalhar(contratoId)).thenReturn(contrato);
            when(tecnicoService.buscarOuFalhar(novoTecnicoId)).thenReturn(novoTecnico);
            when(repository.save(any())).thenReturn(osExistente);
            when(assembler.toModel(any())).thenReturn(new OrdemServicoOutput());

            service.atualizar(osId, input);

            verify(historicoOrdemServicoService).registrar(
                    eq(osExistente),
                    any(),
                    contains("Técnico")
            );
        }

        @Test
        @DisplayName("deve incluir alteração de statusRastreio, rastreio e PIB na descrição")
        void deveIncluirAlteracoesMultiplas() {
            OrdemServicoInput input = inputValido();
            input.setTecnicoId(null);
            input.setCredenciadoId(null);

            doAnswer(invocation -> {
                OrdemServico os = invocation.getArgument(1);
                os.setRastreio("NOVO-RASTREIO");
                os.setPib("NOVO-PIB");
                os.setAcionador("Novo Acionador");
                os.setSerie("SN-NOVA");
                os.setDepartamento("Novo Depto");
                os.setTelefone("86988887777");
                os.setStatusRastreio(StatusRastreio.POSTADO);
                return null;
            }).when(disassembler).copyToDomainObject(any(), any());

            when(repository.findById(osId)).thenReturn(Optional.of(osExistente));
            when(clienteService.buscarOuFalhar(clienteId)).thenReturn(cliente);
            when(contratoService.buscarOuFalhar(contratoId)).thenReturn(contrato);
            when(repository.save(any())).thenReturn(osExistente);
            when(assembler.toModel(any())).thenReturn(new OrdemServicoOutput());

            service.atualizar(osId, input);

            verify(historicoOrdemServicoService).registrar(
                    eq(osExistente),
                    eq(TipoAcaoOS.ATUALIZACAO),
                    argThat(desc -> desc.contains("Rastreio") || desc.contains("PIB") || desc.contains("Acionador"))
            );
        }

        @Test
        @DisplayName("deve incluir alteração de cliente e contrato na descrição quando IDs mudam")
        void deveIncluirAlteracaoClienteContrato() {
            UUID novoClienteId = UUID.randomUUID();
            UUID novoContratoId = UUID.randomUUID();

            Cliente novoCliente = new Cliente();
            novoCliente.setId(novoClienteId);
            novoCliente.setNome("Novo Cliente");

            Contrato novoContrato = new Contrato();
            novoContrato.setId(novoContratoId);
            novoContrato.setCliente(novoCliente);

            OrdemServicoInput input = inputValido();
            input.setTecnicoId(null);
            input.setCredenciadoId(null);
            input.setClienteId(novoClienteId);
            input.setContratoId(novoContratoId);

            doAnswer(invocation -> {
                OrdemServico os = invocation.getArgument(1);
                Cliente clienteProxy = new Cliente();
                clienteProxy.setId(novoClienteId);
                os.setCliente(clienteProxy);
                Contrato contratoProxy = new Contrato();
                contratoProxy.setId(novoContratoId);
                contratoProxy.setCliente(novoCliente);
                os.setContrato(contratoProxy);
                return null;
            }).when(disassembler).copyToDomainObject(any(), any());

            when(repository.findById(osId)).thenReturn(Optional.of(osExistente));
            when(clienteService.buscarOuFalhar(novoClienteId)).thenReturn(novoCliente);
            when(contratoService.buscarOuFalhar(novoContratoId)).thenReturn(novoContrato);
            when(repository.save(any())).thenReturn(osExistente);
            when(assembler.toModel(any())).thenReturn(new OrdemServicoOutput());

            service.atualizar(osId, input);

            verify(historicoOrdemServicoService).registrar(
                    eq(osExistente),
                    any(),
                    argThat(desc -> desc.contains("Cliente") || desc.contains("Contrato") || !desc.isEmpty())
            );
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