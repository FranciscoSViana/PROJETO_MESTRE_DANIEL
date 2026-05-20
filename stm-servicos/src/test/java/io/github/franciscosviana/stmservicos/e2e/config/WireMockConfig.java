package io.github.franciscosviana.stmservicos.e2e.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

public class WireMockConfig {

    static final WireMockServer WIRE_MOCK_SERVER =
            new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());

    static {
        WIRE_MOCK_SERVER.start();
        configurarStubsPadrao();
    }

    @DynamicPropertySource
    static void configureWireMock(DynamicPropertyRegistry registry) {
        String baseUrl = "http://localhost:" + WIRE_MOCK_SERVER.port();
        registry.add("viacep.base-url", () -> baseUrl);
        registry.add("brasilapi.base-url", () -> baseUrl);
        registry.add("googlemaps.base-url", () -> baseUrl);
    }

    static WireMockServer getServer() {
        return WIRE_MOCK_SERVER;
    }

    static void resetStubs() {
        WIRE_MOCK_SERVER.resetAll();
        configurarStubsPadrao();
    }

    private static void configurarStubsPadrao() {
        // Stub padrão para ViaCEP
        WIRE_MOCK_SERVER.stubFor(
            WireMock.get(WireMock.urlMatching("/ws/.*/json"))
                .willReturn(WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                        {
                            "cep": "64000-000",
                            "logradouro": "Avenida Frei Serafim",
                            "bairro": "Centro",
                            "localidade": "Teresina",
                            "uf": "PI",
                            "latitude": "-5.0919",
                            "longitude": "-42.8034"
                        }
                        """)));

        // Stub padrão para Google Maps Geocoding
        WIRE_MOCK_SERVER.stubFor(
            WireMock.get(WireMock.urlPathMatching("/maps/api/geocode/json"))
                .willReturn(WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                        {
                            "status": "OK",
                            "results": [{
                                "geometry": {
                                    "location": {
                                        "lat": -5.0919,
                                        "lng": -42.8034
                                    }
                                },
                                "address_components": [{
                                    "types": ["administrative_area_level_1"],
                                    "short_name": "PI"
                                }]
                            }]
                        }
                        """)));

        // Stub padrão para BrasilAPI — CNPJ
        WIRE_MOCK_SERVER.stubFor(
            WireMock.get(WireMock.urlMatching("/api/cnpj/v1/.*"))
                .willReturn(WireMock.aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("""
                        {
                            "cnpj": "12345678000190",
                            "razao_social": "EMPRESA TESTE LTDA",
                            "nome_fantasia": "Empresa Teste",
                            "logradouro": "Rua Principal",
                            "numero": "100",
                            "bairro": "Centro",
                            "municipio": "Teresina",
                            "uf": "PI",
                            "cep": "64000000"
                        }
                        """)));
    }
}
