package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.assembler.ContratoInputDisassembler;
import io.github.franciscosviana.stmservicos.api.assembler.ContratoOutputAssembler;
import io.github.franciscosviana.stmservicos.api.model.input.ContratoInput;
import io.github.franciscosviana.stmservicos.api.model.output.ContratoOutput;
import io.github.franciscosviana.stmservicos.common.validation.ClienteException;
import io.github.franciscosviana.stmservicos.common.validation.ContratoException;
import io.github.franciscosviana.stmservicos.domain.model.Cliente;
import io.github.franciscosviana.stmservicos.domain.model.Contrato;
import io.github.franciscosviana.stmservicos.domain.repository.ClienteRepository;
import io.github.franciscosviana.stmservicos.domain.repository.ContratoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContratoService")
class ContratoServiceTest {

    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private ContratoRepository contratoRepository;
    @Mock
    private ContratoInputDisassembler contratoDisassembler;
    @Mock
    private ContratoOutputAssembler contratoAssembler;

    @InjectMocks
    private ContratoService service;

    private UUID clienteId;
    private UUID contratoId;
    private Cliente cliente;
    private Contrato contrato;

    @BeforeEach
    void setUp() {
        clienteId = UUID.randomUUID();
        contratoId = UUID.randomUUID();

        cliente = new Cliente();
        cliente.setId(clienteId);
        cliente.setNome("Empresa Teste");

        contrato = new Contrato();
        contrato.setId(contratoId);
        contrato.setCliente(cliente);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // adicionar()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("adicionar()")
    class Adicionar {

        @Test
        @DisplayName("deve adicionar contrato ao cliente com sucesso")
        void deveAdicionarContrato() {
            ContratoInput input = new ContratoInput();
            ContratoOutput output = new ContratoOutput();

            when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
            when(contratoDisassembler.toDomainObject(input)).thenReturn(contrato);
            when(contratoRepository.save(contrato)).thenReturn(contrato);
            when(contratoAssembler.toModel(contrato)).thenReturn(output);

            assertThat(service.adicionar(clienteId, input)).isEqualTo(output);
            assertThat(contrato.getCliente()).isEqualTo(cliente);
        }

        @Test
        @DisplayName("deve lançar ClienteException quando cliente não encontrado")
        void deveLancarExcecaoSeClienteNaoEncontrado() {
            when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.adicionar(clienteId, new ContratoInput()))
                    .isInstanceOf(ClienteException.class)
                    .hasMessageContaining("não encontrado");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // listar()
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("listar() deve retornar contratos do cliente")
    void deveListarContratos() {
        when(contratoRepository.findByClienteId(clienteId)).thenReturn(List.of(contrato));
        when(contratoAssembler.toModel(contrato)).thenReturn(new ContratoOutput());

        var resultado = service.listar(clienteId);

        assertThat(resultado).hasSize(1);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // atualizar()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("atualizar()")
    class Atualizar {

        @Test
        @DisplayName("deve atualizar contrato com sucesso")
        void deveAtualizar() {
            ContratoInput input = new ContratoInput();
            ContratoOutput output = new ContratoOutput();

            when(contratoRepository.findByIdAndClienteId(contratoId, clienteId))
                    .thenReturn(Optional.of(contrato));
            doNothing().when(contratoDisassembler).copyToDomainObject(input, contrato);
            when(contratoAssembler.toModel(contrato)).thenReturn(output);

            assertThat(service.atualizar(clienteId, contratoId, input)).isEqualTo(output);
        }

        @Test
        @DisplayName("deve lançar exceção quando contrato não pertence ao cliente")
        void deveLancarExcecaoSeContratoNaoPertenceAoCliente() {
            when(contratoRepository.findByIdAndClienteId(contratoId, clienteId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.atualizar(clienteId, contratoId, new ContratoInput()))
                    .isInstanceOf(ClienteException.class)
                    .hasMessageContaining("Contrato não encontrado");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // excluir()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("excluir()")
    class Excluir {

        @Test
        @DisplayName("deve excluir contrato com sucesso")
        void deveExcluir() {
            when(contratoRepository.findByIdAndClienteId(contratoId, clienteId))
                    .thenReturn(Optional.of(contrato));

            service.excluir(clienteId, contratoId);

            verify(contratoRepository).delete(contrato);
        }

        @Test
        @DisplayName("deve lançar exceção quando contrato não encontrado")
        void deveLancarExcecaoSeNaoEncontrado() {
            when(contratoRepository.findByIdAndClienteId(contratoId, clienteId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.excluir(clienteId, contratoId))
                    .isInstanceOf(ClienteException.class);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // buscarOuFalhar()
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("buscarOuFalhar() deve lançar ContratoException quando não encontrado")
    void deveLancarContratoException() {
        when(contratoRepository.findById(contratoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarOuFalhar(contratoId))
                .isInstanceOf(ContratoException.class)
                .hasMessageContaining("Contrato não encontrado");
    }
}
