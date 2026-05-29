package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.assembler.CredenciadoInputDisassembler;
import io.github.franciscosviana.stmservicos.api.assembler.CredenciadoOutputAssembler;
import io.github.franciscosviana.stmservicos.api.model.input.CredenciadoInput;
import io.github.franciscosviana.stmservicos.api.model.output.CredenciadoOutput;
import io.github.franciscosviana.stmservicos.common.client.BrasilAPIClient;
import io.github.franciscosviana.stmservicos.common.client.GoogleMapsClient;
import io.github.franciscosviana.stmservicos.common.client.model.EstadoResponse;
import io.github.franciscosviana.stmservicos.common.client.model.MunicipioResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CredenciadoService")
class CredenciadoServiceTest {

    @Mock private BrasilAPIClient brasilAPIClient;
    @Mock private GoogleMapsClient googleMapsClient;
    @Mock private CredenciadoOutputAssembler assembler;
    @Mock private CredenciadoInputDisassembler disassembler;
    @Mock private CredenciadoRepository credenciadoRepository;

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
            CredenciadoInput input = inputCredenciado(null, null);
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
            CredenciadoInput input = inputCredenciado(2, "11.111.111/0001-11");

            assertThatThrownBy(() -> service.salvar(input))
                    .isInstanceOf(CredenciadoException.class)
                    .hasMessageContaining("inválido");
        }

        @Test
        @DisplayName("deve lançar CredenciadoException com CPF inválido (tipo pessoa física)")
        void deveLancarExcecaoComCpfInvalido() {
            CredenciadoInput input = inputCredenciado(1, "000.000.000-00");

            assertThatThrownBy(() -> service.salvar(input))
                    .isInstanceOf(CredenciadoException.class)
                    .hasMessageContaining("inválido");
        }

        @Test
        @DisplayName("deve aceitar CPF válido e prosseguir com salvamento")
        void deveAceitarCpfValido() {
            CredenciadoInput input = inputCredenciado(1, "529.982.247-25");
            GeoLocation geo = new GeoLocation(-5.1, -42.8);

            when(credenciadoRepository.buscarUltimoCodigo()).thenReturn(0L);
            when(disassembler.toDomainObject(input)).thenReturn(credenciado);
            when(googleMapsClient.buscarPorEnderecoComValidacao(anyString(), anyString())).thenReturn(geo);
            when(credenciadoRepository.save(any())).thenReturn(credenciado);
            when(assembler.toModel(credenciado)).thenReturn(new CredenciadoOutput());

            CredenciadoOutput result = service.salvar(input);

            assertThat(result).isNotNull();
            verify(credenciadoRepository).save(any());
        }

        @Test
        @DisplayName("deve aceitar CNPJ válido e prosseguir com salvamento")
        void deveAceitarCnpjValido() {
            CredenciadoInput input = inputCredenciado(2, "11.222.333/0001-81");
            GeoLocation geo = new GeoLocation(-5.1, -42.8);

            when(credenciadoRepository.buscarUltimoCodigo()).thenReturn(0L);
            when(disassembler.toDomainObject(input)).thenReturn(credenciado);
            when(googleMapsClient.buscarPorEnderecoComValidacao(anyString(), anyString())).thenReturn(geo);
            when(credenciadoRepository.save(any())).thenReturn(credenciado);
            when(assembler.toModel(credenciado)).thenReturn(new CredenciadoOutput());

            CredenciadoOutput result = service.salvar(input);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("deve lançar IllegalArgumentException para código de tipo de pessoa inválido")
        void deveLancarExcecaoParaTipoPessoaInvalido() {
            CredenciadoInput input = inputCredenciado(99, "123.456.789-00");

            assertThatThrownBy(() -> service.salvar(input))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // atualizar()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("atualizar()")
    class Atualizar {

        @Test
        @DisplayName("deve atualizar credenciado com sucesso")
        void deveAtualizarComSucesso() {
            CredenciadoInput input = inputCredenciado(null, null);
            GeoLocation geo = new GeoLocation(-5.1, -42.8);

            when(credenciadoRepository.findById(credenciadoId)).thenReturn(Optional.of(credenciado));
            doNothing().when(disassembler).copyToDomainObject(eq(input), eq(credenciado));
            when(googleMapsClient.buscarPorEnderecoComValidacao(anyString(), anyString())).thenReturn(geo);
            when(credenciadoRepository.save(any())).thenReturn(credenciado);
            when(assembler.toModel(credenciado)).thenReturn(new CredenciadoOutput());

            CredenciadoOutput result = service.atualizar(credenciadoId, input);

            assertThat(result).isNotNull();
            verify(credenciadoRepository).save(credenciado);
        }

        @Test
        @DisplayName("deve lançar CredenciadoException quando credenciado não encontrado")
        void deveLancarExcecaoSeNaoEncontrado() {
            when(credenciadoRepository.findById(credenciadoId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.atualizar(credenciadoId, inputCredenciado(null, null)))
                    .isInstanceOf(CredenciadoException.class)
                    .hasMessageContaining("não encontrado");
        }

        @Test
        @DisplayName("deve usar CEP como fallback na geolocalização durante atualização")
        void deveUsarCepComoFallbackNaAtualizacao() {
            CredenciadoInput input = inputCredenciado(null, null);
            GeoLocation geoCep = new GeoLocation(-5.3, -43.0);

            when(credenciadoRepository.findById(credenciadoId)).thenReturn(Optional.of(credenciado));
            doNothing().when(disassembler).copyToDomainObject(eq(input), eq(credenciado));
            when(googleMapsClient.buscarPorEnderecoComValidacao(contains("Av. Principal"), anyString()))
                    .thenThrow(new RuntimeException("Não encontrado"));
            when(googleMapsClient.buscarPorEnderecoComValidacao(contains("64000-000"), anyString()))
                    .thenReturn(geoCep);
            when(credenciadoRepository.save(any())).thenReturn(credenciado);
            when(assembler.toModel(credenciado)).thenReturn(new CredenciadoOutput());

            service.atualizar(credenciadoId, input);

            assertThat(credenciado.getGeoLocation()).isEqualTo(geoCep);
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

        @Test
        @DisplayName("buscarPorCodigo() deve retornar output quando encontrado")
        void buscarPorCodigoDeveRetornar() {
            CredenciadoOutput output = new CredenciadoOutput();
            when(credenciadoRepository.findByCodigo(1L)).thenReturn(Optional.of(credenciado));
            when(assembler.toModel(credenciado)).thenReturn(output);

            assertThat(service.buscarPorCodigo(1L)).isEqualTo(output);
        }

        @Test
        @DisplayName("buscarPorCodigo() deve lançar RuntimeException quando não encontrado")
        void buscarPorCodigoDeveLancarExcecao() {
            when(credenciadoRepository.findByCodigo(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPorCodigo(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("999");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // listar() e listarComFiltro()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("listar()")
    class Listar {

        @Test
        @DisplayName("listar() deve retornar página mapeada")
        void deveListarPaginado() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Credenciado> page = new PageImpl<>(List.of(credenciado));
            CredenciadoOutput output = new CredenciadoOutput();

            when(credenciadoRepository.findAll(pageable)).thenReturn(page);
            when(assembler.toModel(credenciado)).thenReturn(output);

            Page<CredenciadoOutput> resultado = service.listar(pageable);

            assertThat(resultado.getContent()).hasSize(1);
            assertThat(resultado.getContent().get(0)).isEqualTo(output);
        }

        @Test
        @DisplayName("listarComFiltro() deve aplicar specification e retornar página")
        @SuppressWarnings("unchecked")
        void deveListarComFiltro() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Credenciado> page = new PageImpl<>(List.of(credenciado));
            CredenciadoOutput output = new CredenciadoOutput();

            when(credenciadoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
            when(assembler.toModel(credenciado)).thenReturn(output);

            Page<CredenciadoOutput> resultado = service.listarComFiltro(1L, "Credenciado", null, "Teresina", "PI", pageable);

            assertThat(resultado.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("listarComFiltro() sem filtros deve retornar todos os registros")
        @SuppressWarnings("unchecked")
        void deveListarSemFiltros() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Credenciado> page = new PageImpl<>(List.of(credenciado));

            when(credenciadoRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
            when(assembler.toModel(credenciado)).thenReturn(new CredenciadoOutput());

            Page<CredenciadoOutput> resultado = service.listarComFiltro(null, null, null, null, null, pageable);

            assertThat(resultado).isNotNull();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // buscarProximosPorCep()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("buscarProximosPorCep()")
    class BuscarProximosPorCep {

        @Test
        @DisplayName("deve retornar credenciados próximos quando geolocalização encontrada")
        void deveRetornarCredenciadosProximos() {
            GeoLocation origem = new GeoLocation(-5.1, -42.8);
            CredenciadoOutput output = new CredenciadoOutput();

            when(googleMapsClient.buscarPorEndereco(anyString())).thenReturn(origem);
            when(credenciadoRepository.buscarPorRaio(anyDouble(), anyDouble(), anyDouble()))
                    .thenReturn(List.of(credenciado));
            when(assembler.toModel(credenciado)).thenReturn(output);

            List<CredenciadoOutput> resultado = service.buscarProximosPorCep("64000-000", 50.0);

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0)).isEqualTo(output);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando nenhum credenciado no raio")
        void deveRetornarListaVaziaQuandoSemCredenciados() {
            GeoLocation origem = new GeoLocation(-5.1, -42.8);

            when(googleMapsClient.buscarPorEndereco(anyString())).thenReturn(origem);
            when(credenciadoRepository.buscarPorRaio(anyDouble(), anyDouble(), anyDouble()))
                    .thenReturn(List.of());

            List<CredenciadoOutput> resultado = service.buscarProximosPorCep("64000-000", 50.0);

            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("deve lançar RuntimeException quando geolocalização do CEP falhar")
        void deveLancarExcecaoQuandoGeoFalhar() {
            when(googleMapsClient.buscarPorEndereco(anyString()))
                    .thenThrow(new RuntimeException("Serviço indisponível"));

            assertThatThrownBy(() -> service.buscarProximosPorCep("00000-000", 50.0))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("geolocalização");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // atualizarGeoTodos()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("atualizarGeoTodos()")
    class AtualizarGeoTodos {

        @Test
        @DisplayName("deve geocodificar credenciados sem geolocalização")
        void deveGeocodificarCredenciados() {
            credenciado.setGeoLocation(null);
            GeoLocation geo = new GeoLocation(-5.1, -42.8);

            when(credenciadoRepository.findAll()).thenReturn(List.of(credenciado));
            when(googleMapsClient.buscarPorEndereco(anyString())).thenReturn(geo);

            service.atualizarGeoTodos();

            assertThat(credenciado.getGeoLocation()).isEqualTo(geo);
        }

        @Test
        @DisplayName("deve ignorar credenciados que já têm geolocalização")
        void deveIgnorarCredenciadosComGeo() {
            GeoLocation geoExistente = new GeoLocation(-5.0, -42.0);
            credenciado.setGeoLocation(geoExistente);

            when(credenciadoRepository.findAll()).thenReturn(List.of(credenciado));

            service.atualizarGeoTodos();

            verify(googleMapsClient, never()).buscarPorEndereco(anyString());
            assertThat(credenciado.getGeoLocation()).isEqualTo(geoExistente);
        }

        @Test
        @DisplayName("deve continuar processando quando geocodificação de um credenciado falha")
        void deveContinuarQuandoGeoFalha() {
            credenciado.setGeoLocation(null);

            when(credenciadoRepository.findAll()).thenReturn(List.of(credenciado));
            when(googleMapsClient.buscarPorEndereco(anyString()))
                    .thenThrow(new RuntimeException("Falha na geocodificação"));

            service.atualizarGeoTodos();

            assertThat(credenciado.getGeoLocation()).isNull();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // listarEstados() e listarMunicipios()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("listarEstados() e listarMunicipios()")
    class ListarEstadosMunicipios {

        @Test
        @DisplayName("listarEstados() deve retornar lista ordenada de estados")
        void deveListarEstadosOrdenados() {
            EstadoResponse pi = new EstadoResponse();
            pi.setNome("Piauí");
            pi.setSigla("PI");

            EstadoResponse ba = new EstadoResponse();
            ba.setNome("Bahia");
            ba.setSigla("BA");

            when(brasilAPIClient.buscarEstados()).thenReturn(List.of(pi, ba));

            List<EstadoResponse> resultado = service.listarEstados();

            assertThat(resultado).hasSize(2);
            assertThat(resultado.get(0).getNome()).isEqualTo("Bahia");
            assertThat(resultado.get(1).getNome()).isEqualTo("Piauí");
        }

        @Test
        @DisplayName("listarMunicipios() deve retornar municípios da UF informada")
        void deveListarMunicipios() {
            EstadoResponse pi = new EstadoResponse();
            pi.setSigla("PI");
            pi.setNome("Piauí");

            MunicipioResponse mun = new MunicipioResponse();

            when(brasilAPIClient.buscarEstados()).thenReturn(List.of(pi));
            when(brasilAPIClient.buscarMunicipios("PI")).thenReturn(List.of(mun));

            List<MunicipioResponse> resultado = service.listarMunicipios("PI");

            assertThat(resultado).hasSize(1);
        }

        @Test
        @DisplayName("listarMunicipios() deve lançar RuntimeException para UF inválida")
        void deveLancarExcecaoUfInvalida() {
            EstadoResponse pi = new EstadoResponse();
            pi.setSigla("PI");
            pi.setNome("Piauí");

            when(brasilAPIClient.buscarEstados()).thenReturn(List.of(pi));

            assertThatThrownBy(() -> service.listarMunicipios("XX"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("XX");
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