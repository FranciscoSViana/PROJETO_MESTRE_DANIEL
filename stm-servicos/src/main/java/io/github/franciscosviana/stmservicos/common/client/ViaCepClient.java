package io.github.franciscosviana.stmservicos.common.client;

import io.github.franciscosviana.stmservicos.common.client.model.ViaCepResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class ViaCepClient {

    private static final String VIACEP = "https://viacep.com.br/ws";

    private final RestTemplate restTemplate;

    public ViaCepResponse buscarEnderecoPorCep(String cep) {

        String url = VIACEP + "/" + cep + "/json/";

        try {
            return restTemplate.getForObject(url, ViaCepResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao consultar endereço no ViaCEP: " + e.getMessage());
        }
    }
}
