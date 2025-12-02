package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.common.client.ViaCepClient;
import io.github.franciscosviana.stmservicos.common.client.model.ViaCepResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnderecoService {

    private final ViaCepClient viaCepClient;

    public ViaCepResponse consultarCep(String cep) {
        return viaCepClient.buscarEnderecoPorCep(cep);
    }
}
