package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.assembler.CredenciadoInputDisassembler;
import io.github.franciscosviana.stmservicos.api.assembler.CredenciadoOutputAssembler;
import io.github.franciscosviana.stmservicos.api.model.input.CredenciadoInput;
import io.github.franciscosviana.stmservicos.api.model.output.CredenciadoOutput;
import io.github.franciscosviana.stmservicos.common.client.BrasilAPIClient;
import io.github.franciscosviana.stmservicos.common.client.GoogleMapsClient;
import io.github.franciscosviana.stmservicos.common.validation.CredenciadoException;
import io.github.franciscosviana.stmservicos.domain.model.Credenciado;
import io.github.franciscosviana.stmservicos.domain.model.Endereco;
import io.github.franciscosviana.stmservicos.domain.model.GeoLocation;
import io.github.franciscosviana.stmservicos.domain.repository.CredenciadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CredenciadoService")
class CredenciadoServiceTest {

    @Mock
    private BrasilAPIClient brasilAPIClient;
    @Mock
    private GoogleMapsClient googleMapsClient;
    @Mock
    private CredenciadoOutputAssembler assembler;
    @Mock
    private CredenciadoInputDisassembler disassembler;
    @Mock
    private CredenciadoRepository credenciadoRepository;

    @InjectMocks
    private CredenciadoService service;

    private UUID credenciadoId;
    private Credenciado credenciado;

    @BeforeEach
    void setUp() {
        credenciadoId = UUID.randomUUID();

        Endereco endereco = Endereco.builder()
                .logradouro("Av. Principal")
                .numero("100")
                .bairro("Centro")
                .cidade("Teresina")
                .estado("PI")
                .cep("64000-000")
                .build();

        credenciado = new Credenciado();
        credenciado.setId(credenciadoId);
        credenciado.setCodigo(1L);
        credenciado.setRag("Credenciado Ltda");
        credenciado.setEndereco(endereco);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // salvar()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("salvar()")
    class Salvar {

        @Test
        @DisplayName("deve salvar credenciado com geolocalização com sucesso")
        void deveSalvarComSucesso() {
            CredenciadoInput input = inputCredenciado(null, null); // sem número de pessoa
            GeoLocation geo = new GeoLocation(-5.1, -42.8);

            when(credenciadoRepository.buscarUltimoCodigo()).thenReturn(0L);
            when(disassembler.toDomainObject(input)).thenReturn(credenciado);
            when(googleMapsClient.buscarPorEnderecoComValidacao(anyString(), anyString())).thenReturn(geo);
            when(credenciadoRepository.save(any())).thenReturn(credenciado);
            when(assembler.toModel(credenciado)).thenReturn(new CredenciadoOutput());

            service.salvar(input);

            assertThat(credenciado.getGeoLocation()).isEqualTo(geo);
            assertThat(credenciado.getCodigo()).isEqualTo(1L);
            verify(credenciadoRepository).save(credenciado);
        }

        @Test
        @DisplayName("deve usar geocodificação por CEP como fallback quando endereço não encontrado")
        void deveUsarCepComoFallback() {
            CredenciadoInput input = inputCredenciado(null, null);
            GeoLocation geoCep = new GeoLocation(-5.2, -42.9);

            when(credenciadoRepository.buscarUltimoCodigo()).thenReturn(0L);
            when(disassembler.toDomainObject(input)).thenReturn(credenciado);
            when(googleMapsClient.buscarPorEnderecoComValidacao(contains("Av. Principal"), anyString()))
                    .thenThrow(new RuntimeException("Endereço não encontrado"));
            when(googleMapsClient.buscarPorEnderecoComValidacao(contains("64000-000"), anyString()))
                    .thenReturn(geoCep);
            when(credenciadoRepository.save(any())).thenReturn(credenciado);
            when(assembler.toModel(credenciado)).thenReturn(new CredenciadoOutput());

            service.salvar(input);

            assertThat(credenciado.getGeoLocation()).isEqualTo(geoCep);
        }

        @Test
        @DisplayName("deve lançar CredenciadoException quando nem endereço nem CEP têm geolocalização")
        void deveLancarExcecaoSemGeolocalizacao() {
            CredenciadoInput input = inputCredenciado(null, null);

            when(credenciadoRepository.buscarUltimoCodigo()).thenReturn(0L);
            when(disassembler.toDomainObject(input)).thenReturn(credenciado);
            when(googleMapsClient.buscarPorEnderecoComValidacao(anyString(), anyString()))
                    .thenThrow(new RuntimeException("Sem resultado"));

            assertThatThrownBy(() -> service.salvar(input))
                    .isInstanceOf(CredenciadoException.class)
                    .hasMessageContaining("geolocalização");
        }

        @Test
        @DisplayName("deve lançar CredenciadoException com CNPJ inválido (tipo pessoa jurídica)")
        void deveLancarExcecaoComCnpjInvalido() {
            // tipo 2 = PJ, CNPJ inválido
            CredenciadoInput input = inputCredenciado(2, "11.111.111/0001-11");

            assertThatThrownBy(() -> service.salvar(input))
                    .isInstanceOf(CredenciadoException.class)
                    .hasMessageContaining("inválido");
        }

        @Test
        @DisplayName("deve lançar CredenciadoException com CPF inválido (tipo pessoa física)")
        void deveLancarExcecaoComCpfInvalido() {
            // tipo 1 = PF, CPF inválido
            CredenciadoInput input = inputCredenciado(1, "000.000.000-00");

            assertThatThrownBy(() -> service.salvar(input))
                    .isInstanceOf(CredenciadoException.class)
                    .hasMessageContaining("inválido");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // excluir()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("excluir()")
    class Excluir {

        @Test
        @DisplayName("deve excluir credenciado com sucesso")
        void deveExcluir() {
            when(credenciadoRepository.findById(credenciadoId)).thenReturn(Optional.of(credenciado));

            service.excluir(credenciadoId);

            verify(credenciadoRepository).delete(credenciado);
        }

        @Test
        @DisplayName("deve lançar CredenciadoException quando não encontrado")
        void deveLancarExcecao() {
            when(credenciadoRepository.findById(credenciadoId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.excluir(credenciadoId))
                    .isInstanceOf(CredenciadoException.class)
                    .hasMessageContaining("não encontrado");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // buscarPorId() e buscarPorCodigo()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("buscar()")
    class Buscar {

        @Test
        @DisplayName("buscarPorId() deve retornar output quando encontrado")
        void buscarPorIdDeveRetornar() {
            CredenciadoOutput output = new CredenciadoOutput();
            when(credenciadoRepository.findById(credenciadoId)).thenReturn(Optional.of(credenciado));
            when(assembler.toModel(credenciado)).thenReturn(output);

            assertThat(service.buscarPorId(credenciadoId)).isEqualTo(output);
        }

        @Test
        @DisplayName("buscarOuFalhar() deve lançar CredenciadoException quando não encontrado")
        void buscarOuFalharDeveLancarExcecao() {
            when(credenciadoRepository.findById(credenciadoId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarOuFalhar(credenciadoId))
                    .isInstanceOf(CredenciadoException.class)
                    .hasMessageContaining("não encontrado");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────
    private CredenciadoInput inputCredenciado(Integer tipoPessoa, String numeroPessoa) {
        CredenciadoInput input = new CredenciadoInput();
        input.setRag("Credenciado Ltda");
        input.setTipoPessoa(tipoPessoa);
        input.setNumeroPessoa(numeroPessoa);
        return input;
    }
}
