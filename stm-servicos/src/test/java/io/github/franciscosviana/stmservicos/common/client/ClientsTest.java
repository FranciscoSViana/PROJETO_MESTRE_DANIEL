package io.github.franciscosviana.stmservicos.common.client;

import io.github.franciscosviana.stmservicos.common.client.model.*;
import io.github.franciscosviana.stmservicos.common.validation.CepSemGeolocalizacaoException;
import io.github.franciscosviana.stmservicos.domain.model.GeoLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("API Clients")
class ClientsTest {

    // ──────────────────────────────────────────────────────────────────────────
    // ViaCepClient
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("ViaCepClient")
    class ViaCepClientTest {

        @Mock RestTemplate restTemplate;
        @InjectMocks ViaCepClient client;

        @Test
        @DisplayName("deve retornar ViaCepResponse quando CEP válido")
        void deveRetornarResponseParaCepValido() {
            ViaCepResponse resp = new ViaCepResponse();
            resp.setCep("64000-000");
            resp.setLogradouro("Av. Frei Serafim");

            when(restTemplate.getForObject(anyString(), eq(ViaCepResponse.class))).thenReturn(resp);

            ViaCepResponse resultado = client.buscarEnderecoPorCep("64000-000");

            assertThat(resultado.getCep()).isEqualTo("64000-000");
            assertThat(resultado.getLogradouro()).isEqualTo("Av. Frei Serafim");
        }

        @Test
        @DisplayName("deve lançar IllegalArgumentException para CEP inválido (menos de 8 dígitos)")
        void deveLancarExcecaoParaCepInvalido() {
            assertThatThrownBy(() -> client.buscarEnderecoPorCep("123"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("CEP inválido");
        }

        @Test
        @DisplayName("deve lançar RuntimeException quando RestTemplate falha")
        void deveLancarRuntimeExceptionQuandoRestTemplateFalha() {
            when(restTemplate.getForObject(anyString(), eq(ViaCepResponse.class)))
                    .thenThrow(new RuntimeException("Timeout"));

            assertThatThrownBy(() -> client.buscarEnderecoPorCep("64000000"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("ViaCEP");
        }

        @Test
        @DisplayName("deve remover máscara do CEP antes de consultar")
        void deveRemoverMascaraDoCep() {
            ViaCepResponse resp = new ViaCepResponse();
            when(restTemplate.getForObject(contains("64000000"), eq(ViaCepResponse.class))).thenReturn(resp);

            client.buscarEnderecoPorCep("64000-000");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // BrasilAPIClient
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("BrasilAPIClient")
    class BrasilAPIClientTest {

        @Mock RestTemplate restTemplate;
        @InjectMocks BrasilAPIClient client;

        @Test
        @DisplayName("consultarCnpj() deve retornar response para CNPJ válido")
        void deveConsultarCnpjValido() {
            ReceitaWsResponse resp = new ReceitaWsResponse();
            when(restTemplate.getForObject(anyString(), eq(ReceitaWsResponse.class))).thenReturn(resp);

            ReceitaWsResponse resultado = client.consultarCnpj("11.222.333/0001-81");

            assertThat(resultado).isEqualTo(resp);
        }

        @Test
        @DisplayName("consultarCnpj() deve lançar IllegalArgumentException para CNPJ inválido")
        void deveLancarExcecaoParaCnpjInvalido() {
            assertThatThrownBy(() -> client.consultarCnpj("123"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("CNPJ inválido");
        }

        @Test
        @DisplayName("consultarCnpj() deve lançar RuntimeException quando RestTemplate falha")
        void deveLancarRuntimeExceptionQuandoCnpjFalha() {
            when(restTemplate.getForObject(anyString(), eq(ReceitaWsResponse.class)))
                    .thenThrow(new RuntimeException("Timeout"));

            assertThatThrownBy(() -> client.consultarCnpj("11222333000181"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("CNPJ");
        }

        @Test
        @DisplayName("buscarEstados() deve retornar lista de estados")
        void deveBuscarEstados() {
            EstadoResponse pi = new EstadoResponse();
            pi.setNome("Piauí");
            pi.setSigla("PI");

            when(restTemplate.getForObject(anyString(), eq(EstadoResponse[].class)))
                    .thenReturn(new EstadoResponse[]{pi});

            List<EstadoResponse> resultado = client.buscarEstados();

            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getSigla()).isEqualTo("PI");
        }

        @Test
        @DisplayName("buscarMunicipios() deve retornar lista de municípios para UF válida")
        void deveBuscarMunicipios() {
            MunicipioResponse mun = new MunicipioResponse();
            mun.setNome("Teresina");

            when(restTemplate.getForObject(anyString(), eq(MunicipioResponse[].class)))
                    .thenReturn(new MunicipioResponse[]{mun});

            List<MunicipioResponse> resultado = client.buscarMunicipios("PI");

            assertThat(resultado).hasSize(1);
        }

        @Test
        @DisplayName("buscarMunicipios() deve lançar IllegalArgumentException para UF inválida")
        void deveLancarExcecaoParaUfInvalida() {
            assertThatThrownBy(() -> client.buscarMunicipios("XYZ"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("UF inválida");
        }

        @Test
        @DisplayName("consultarCep() deve retornar response para CEP válido")
        void deveConsultarCep() {
            CepGeoResponse resp = new CepGeoResponse();
            when(restTemplate.getForObject(anyString(), eq(CepGeoResponse.class))).thenReturn(resp);

            CepGeoResponse resultado = client.consultarCep("64000-000");

            assertThat(resultado).isEqualTo(resp);
        }

        @Test
        @DisplayName("consultarCep() deve lançar IllegalArgumentException para CEP com formato errado")
        void deveLancarExcecaoParaCepInvalido() {
            assertThatThrownBy(() -> client.consultarCep("123"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("CEP inválido");
        }

        @Test
        @DisplayName("consultarCep() deve lançar RuntimeException quando RestTemplate falha")
        void deveLancarRuntimeExceptionQuandoCepFalha() {
            when(restTemplate.getForObject(anyString(), eq(CepGeoResponse.class)))
                    .thenThrow(new RuntimeException("Timeout"));

            assertThatThrownBy(() -> client.consultarCep("64000000"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("CEP");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GoogleMapsClient
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GoogleMapsClient")
    class GoogleMapsClientTest {

        @Mock RestTemplate restTemplate;
        @InjectMocks GoogleMapsClient client;

        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(client, "apiKey", "test-api-key");
        }

        private GoogleGeocodeResponse responseComLocalizacao(double lat, double lng, String locationType) {
            GoogleGeocodeResponse.Location loc = new GoogleGeocodeResponse.Location();
            loc.setLat(lat);
            loc.setLng(lng);

            GoogleGeocodeResponse.Geometry geo = new GoogleGeocodeResponse.Geometry();
            geo.setLocation(loc);
            geo.setLocation_type(locationType);

            GoogleGeocodeResponse.Result result = new GoogleGeocodeResponse.Result();
            result.setGeometry(geo);
            result.setAddress_components(List.of());

            GoogleGeocodeResponse response = new GoogleGeocodeResponse();
            response.setResults(List.of(result));
            return response;
        }

        @Test
        @DisplayName("buscarPorEndereco() deve retornar GeoLocation quando encontrado")
        void deveBuscarPorEndereco() {
            GoogleGeocodeResponse response = responseComLocalizacao(-5.09, -42.80, "ROOFTOP");
            when(restTemplate.getForObject(anyString(), eq(GoogleGeocodeResponse.class))).thenReturn(response);

            GeoLocation resultado = client.buscarPorEndereco("Av. A, Teresina, PI");

            assertThat(resultado.getLatitude()).isEqualTo(-5.09);
            assertThat(resultado.getLongitude()).isEqualTo(-42.80);
        }

        @Test
        @DisplayName("buscarPorEndereco() deve lançar CepSemGeolocalizacaoException quando resposta nula")
        void deveLancarExcecaoQuandoRespostaNula() {
            when(restTemplate.getForObject(anyString(), eq(GoogleGeocodeResponse.class))).thenReturn(null);

            assertThatThrownBy(() -> client.buscarPorEndereco("endereço inválido"))
                    .isInstanceOf(CepSemGeolocalizacaoException.class);
        }

        @Test
        @DisplayName("buscarPorEndereco() deve lançar exceção quando resultado vazio")
        void deveLancarExcecaoQuandoResultadoVazio() {
            GoogleGeocodeResponse response = new GoogleGeocodeResponse();
            response.setResults(List.of());
            when(restTemplate.getForObject(anyString(), eq(GoogleGeocodeResponse.class))).thenReturn(response);

            assertThatThrownBy(() -> client.buscarPorEndereco("endereço"))
                    .isInstanceOf(CepSemGeolocalizacaoException.class);
        }

        @Test
        @DisplayName("buscarPorEndereco() deve logar aviso para geolocalização aproximada")
        void deveLogarAvisoParaLocalizacaoAproximada() {
            GoogleGeocodeResponse response = responseComLocalizacao(-5.09, -42.80, "APPROXIMATE");
            when(restTemplate.getForObject(anyString(), eq(GoogleGeocodeResponse.class))).thenReturn(response);

            GeoLocation resultado = client.buscarPorEndereco("endereço vago");

            assertThat(resultado).isNotNull();
        }

        @Test
        @DisplayName("buscarPorEnderecoComValidacao() deve retornar GeoLocation quando UF corresponde")
        void deveBuscarComValidacaoUfCorresponde() {
            GoogleGeocodeResponse.AddressComponent comp = new GoogleGeocodeResponse.AddressComponent();
            comp.setShort_name("PI");
            comp.setTypes(List.of("administrative_area_level_1"));

            GoogleGeocodeResponse.Location loc = new GoogleGeocodeResponse.Location();
            loc.setLat(-5.09);
            loc.setLng(-42.80);

            GoogleGeocodeResponse.Geometry geo = new GoogleGeocodeResponse.Geometry();
            geo.setLocation(loc);
            geo.setLocation_type("ROOFTOP");

            GoogleGeocodeResponse.Result result = new GoogleGeocodeResponse.Result();
            result.setGeometry(geo);
            result.setAddress_components(List.of(comp));

            GoogleGeocodeResponse response = new GoogleGeocodeResponse();
            response.setResults(List.of(result));

            when(restTemplate.getForObject(anyString(), eq(GoogleGeocodeResponse.class))).thenReturn(response);

            GeoLocation resultado = client.buscarPorEnderecoComValidacao("Av. A, Teresina, PI", "PI");

            assertThat(resultado.getLatitude()).isEqualTo(-5.09);
        }

        @Test
        @DisplayName("buscarPorEnderecoComValidacao() deve lançar exceção quando UF diverge")
        void deveLancarExcecaoQuandoUfDiverge() {
            GoogleGeocodeResponse.AddressComponent comp = new GoogleGeocodeResponse.AddressComponent();
            comp.setShort_name("BA");
            comp.setTypes(List.of("administrative_area_level_1"));

            GoogleGeocodeResponse.Location loc = new GoogleGeocodeResponse.Location();
            loc.setLat(-12.97);
            loc.setLng(-38.50);

            GoogleGeocodeResponse.Geometry geo = new GoogleGeocodeResponse.Geometry();
            geo.setLocation(loc);
            geo.setLocation_type("ROOFTOP");

            GoogleGeocodeResponse.Result result = new GoogleGeocodeResponse.Result();
            result.setGeometry(geo);
            result.setAddress_components(List.of(comp));

            GoogleGeocodeResponse response = new GoogleGeocodeResponse();
            response.setResults(List.of(result));

            when(restTemplate.getForObject(anyString(), eq(GoogleGeocodeResponse.class))).thenReturn(response);

            assertThatThrownBy(() -> client.buscarPorEnderecoComValidacao("Av. A, Teresina, PI", "PI"))
                    .isInstanceOf(CepSemGeolocalizacaoException.class)
                    .hasMessageContaining("BA");
        }

        @Test
        @DisplayName("buscarPorEnderecoComValidacao() deve aceitar quando UF esperada é null")
        void deveAceitarQuandoUfEsperadaNula() {
            GoogleGeocodeResponse response = responseComLocalizacao(-5.09, -42.80, "ROOFTOP");
            when(restTemplate.getForObject(anyString(), eq(GoogleGeocodeResponse.class))).thenReturn(response);

            GeoLocation resultado = client.buscarPorEnderecoComValidacao("Av. A", null);

            assertThat(resultado).isNotNull();
        }
    }
}