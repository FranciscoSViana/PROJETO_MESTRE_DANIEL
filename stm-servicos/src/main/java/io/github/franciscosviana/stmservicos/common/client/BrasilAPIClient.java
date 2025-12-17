package io.github.franciscosviana.stmservicos.common.client;

import io.github.franciscosviana.stmservicos.common.client.model.CepGeoResponse;
import io.github.franciscosviana.stmservicos.common.client.model.EstadoResponse;
import io.github.franciscosviana.stmservicos.common.client.model.MunicipioResponse;
import io.github.franciscosviana.stmservicos.common.client.model.ReceitaWsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BrasilAPIClient {

    private static final String BRASILAPI = "https://brasilapi.com.br/api";

    private final RestTemplate restTemplate;

    public ReceitaWsResponse consultarCnpj(String cnpj) {
        String url = BRASILAPI + "/cnpj/v1/" + cnpj;

        try {
            return restTemplate.getForObject(url, ReceitaWsResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao consultar CNPJ: " + e.getMessage());
        }
    }

    public List<EstadoResponse> buscarEstados() {
        String url = BRASILAPI + "/ibge/uf/v1";

        EstadoResponse[] estados = restTemplate.getForObject(url, EstadoResponse[].class);

        return List.of(estados);
    }

    public List<MunicipioResponse> buscarMunicipios(String uf) {
        String url = BRASILAPI + "/ibge/municipios/v1/" + uf;

        MunicipioResponse[] municipios = restTemplate.getForObject(url, MunicipioResponse[].class);

        return List.of(municipios);
    }

    public CepGeoResponse consultarCep(String cep) {

        String url = BRASILAPI + "/cep/v2/" + cep;

        try {
            return restTemplate.getForObject(url, CepGeoResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao consultar CEP: " + e.getMessage());
        }
    }
}
