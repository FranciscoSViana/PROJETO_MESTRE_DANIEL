package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.assembler.ClienteInputDisassembler;
import io.github.franciscosviana.stmservicos.api.assembler.ClienteOutputAssembler;
import io.github.franciscosviana.stmservicos.api.assembler.ContratoInputDisassembler;
import io.github.franciscosviana.stmservicos.api.model.input.ClienteInput;
import io.github.franciscosviana.stmservicos.api.model.input.ContratoInput;
import io.github.franciscosviana.stmservicos.api.model.input.EnderecoInput;
import io.github.franciscosviana.stmservicos.api.model.output.ClienteOutput;
import io.github.franciscosviana.stmservicos.common.client.BrasilAPIClient;
import io.github.franciscosviana.stmservicos.common.client.model.ReceitaWsResponse;
import io.github.franciscosviana.stmservicos.common.validation.ClienteException;
import io.github.franciscosviana.stmservicos.domain.model.Cliente;
import io.github.franciscosviana.stmservicos.domain.model.Contrato;
import io.github.franciscosviana.stmservicos.domain.model.Endereco;
import io.github.franciscosviana.stmservicos.domain.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClienteService")
class ClienteServiceTest {

    @Mock
    private BrasilAPIClient receitaWsClient;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private ClienteOutputAssembler clienteOutputAssembler;
    @Mock
    private ClienteInputDisassembler clienteInputDisassembler;
    @Mock
    private ContratoInputDisassembler contratoInputDisassembler;

    @InjectMocks
    private ClienteService service;

    private UUID clienteId;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        clienteId = UUID.randomUUID();
        cliente = new Cliente();
        cliente.setId(clienteId);
        cliente.setNome("Empresa Teste");
        cliente.setCodigo(1L);
        cliente.setEndereco(new Endereco());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // salvar()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("salvar()")
    class Salvar {

        @Test
        @DisplayName("deve salvar cliente usando razão social e endereço da Receita Federal")
        void deveSalvarComDadosDaReceita() {
            ClienteInput input = inputCliente();

            ReceitaWsResponse receita = new ReceitaWsResponse();
            receita.setRazaoSocial("EMPRESA LTDA");
            receita.setCep("64000-000");
            receita.setLogradouro("Rua das Flores");
            receita.setBairro("Centro");
            receita.setNumero("100");
            receita.setMunicipio("Teresina");
            receita.setUf("PI");

            when(clienteRepository.buscarUltimoCodigo()).thenReturn(0L);
            when(receitaWsClient.consultarCnpj(input.getCnpj())).thenReturn(receita);
            when(clienteInputDisassembler.toDomainObject(input)).thenReturn(cliente);
            when(clienteRepository.save(any())).thenReturn(cliente);
            when(clienteOutputAssembler.toModel(cliente)).thenReturn(new ClienteOutput());

            service.salvar(input);

            ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
            verify(clienteRepository).save(captor.capture());

            assertThat(captor.getValue().getRazaoSocial()).isEqualTo("EMPRESA LTDA");
            assertThat(captor.getValue().getCodigo()).isEqualTo(1L);
            assertThat(captor.getValue().getEndereco().getCep()).isEqualTo("64000-000");
            assertThat(captor.getValue().getEndereco().getCidade()).isEqualTo("Teresina");
        }

        @Test
        @DisplayName("deve usar endereço do input quando Receita não retorna dados de endereço")
        void deveUsarEnderecoDoInputQuandoReceitaSemEndereco() {
            ClienteInput input = inputCliente();

            ReceitaWsResponse receita = new ReceitaWsResponse();
            receita.setRazaoSocial("EMPRESA LTDA");
            // sem cep, logradouro, etc.

            when(clienteRepository.buscarUltimoCodigo()).thenReturn(5L);
            when(receitaWsClient.consultarCnpj(input.getCnpj())).thenReturn(receita);
            when(clienteInputDisassembler.toDomainObject(input)).thenReturn(cliente);
            when(clienteRepository.save(any())).thenReturn(cliente);
            when(clienteOutputAssembler.toModel(cliente)).thenReturn(new ClienteOutput());

            service.salvar(input);

            ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
            verify(clienteRepository).save(captor.capture());

            // CEP deve vir do input
            assertThat(captor.getValue().getEndereco().getCep()).isEqualTo("64001-000");
            assertThat(captor.getValue().getCodigo()).isEqualTo(6L);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // buscarPorId()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("buscarPorId()")
    class BuscarPorId {

        @Test
        @DisplayName("deve retornar ClienteOutput quando encontrado")
        void deveRetornarCliente() {
            ClienteOutput output = new ClienteOutput();
            when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
            when(clienteOutputAssembler.toModel(cliente)).thenReturn(output);

            assertThat(service.buscarPorId(clienteId)).isEqualTo(output);
        }

        @Test
        @DisplayName("deve lançar ClienteException quando não encontrado")
        void deveLancarExcecaoSeNaoEncontrado() {
            when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPorId(clienteId))
                    .isInstanceOf(ClienteException.class)
                    .hasMessageContaining("não encontrado");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // buscarPorCodigo()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("buscarPorCodigo()")
    class BuscarPorCodigo {

        @Test
        @DisplayName("deve retornar ClienteOutput quando código existir")
        void deveRetornarClientePorCodigo() {
            ClienteOutput output = new ClienteOutput();
            when(clienteRepository.findByCodigo(1L)).thenReturn(Optional.of(cliente));
            when(clienteOutputAssembler.toModel(cliente)).thenReturn(output);

            assertThat(service.buscarPorCodigo(1L)).isEqualTo(output);
        }

        @Test
        @DisplayName("deve lançar ClienteException quando código não encontrado")
        void deveLancarExcecaoSeCodigoNaoEncontrado() {
            when(clienteRepository.findByCodigo(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPorCodigo(99L))
                    .isInstanceOf(ClienteException.class)
                    .hasMessageContaining("código: 99");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // atualizar()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("atualizar()")
    class Atualizar {

        @Test
        @DisplayName("deve atualizar e retornar ClienteOutput")
        void deveAtualizar() {
            ClienteInput input = inputCliente();
            ClienteOutput output = new ClienteOutput();

            when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
            doNothing().when(clienteInputDisassembler).copyToDomainObject(input, cliente);
            when(clienteRepository.save(cliente)).thenReturn(cliente);
            when(clienteOutputAssembler.toModel(cliente)).thenReturn(output);

            assertThat(service.atualizar(clienteId, input)).isEqualTo(output);
            verify(clienteRepository).save(cliente);
        }

        @Test
        @DisplayName("deve lançar exceção quando cliente não encontrado")
        void deveLancarExcecaoSeNaoEncontrado() {
            when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.atualizar(clienteId, inputCliente()))
                    .isInstanceOf(ClienteException.class);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // excluir()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("excluir()")
    class Excluir {

        @Test
        @DisplayName("deve excluir cliente quando encontrado")
        void deveExcluir() {
            when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));

            service.excluir(clienteId);

            verify(clienteRepository).delete(cliente);
        }

        @Test
        @DisplayName("deve lançar exceção quando cliente não encontrado")
        void deveLancarExcecaoSeNaoEncontrado() {
            when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.excluir(clienteId))
                    .isInstanceOf(ClienteException.class);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // listar()
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("listar() deve retornar página de ClienteOutput")
    void deveListar() {
        when(clienteRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(cliente)));
        when(clienteOutputAssembler.toModel(cliente)).thenReturn(new ClienteOutput());

        var resultado = service.listar(PageRequest.of(0, 10));

        assertThat(resultado.getContent()).hasSize(1);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // adicionarContratos()
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("adicionarContratos() deve adicionar contrato à lista do cliente")
    void deveAdicionarContrato() {
        cliente.setContratos(new ArrayList<>());
        ContratoInput contratoInput = new ContratoInput();
        Contrato contrato = new Contrato();

        when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
        when(contratoInputDisassembler.toDomainObject(contratoInput)).thenReturn(contrato);
        when(clienteRepository.save(any())).thenReturn(cliente);
        when(clienteOutputAssembler.toModel(cliente)).thenReturn(new ClienteOutput());

        service.adicionarContratos(clienteId, contratoInput);

        assertThat(cliente.getContratos()).hasSize(1);
        assertThat(contrato.getCliente()).isEqualTo(cliente);
        assertThat(contrato.getId()).isNotNull();
    }

    // ──────────────────────────────────────────────────────────────────────────
    // consultarCnpj()
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("consultarCnpj() deve delegar para BrasilAPIClient")
    void deveConsultarCnpj() {
        ReceitaWsResponse resp = new ReceitaWsResponse();
        when(receitaWsClient.consultarCnpj("12345678000195")).thenReturn(resp);

        assertThat(service.consultarCnpj("12345678000195")).isEqualTo(resp);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────
    private ClienteInput inputCliente() {
        ClienteInput input = new ClienteInput();
        input.setCnpj("12345678000195");
        input.setNome("Empresa Teste");

        EnderecoInput endereco = new EnderecoInput();
        endereco.setCep("64001-000");
        endereco.setLogradouro("Av. Principal");
        endereco.setBairro("Centro");
        endereco.setNumero("200");
        endereco.setCidade("Teresina");
        endereco.setEstado("PI");
        input.setEndereco(endereco);

        return input;
    }
}
