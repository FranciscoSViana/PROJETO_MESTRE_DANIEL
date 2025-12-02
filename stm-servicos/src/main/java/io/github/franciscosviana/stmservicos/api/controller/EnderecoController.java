package io.github.franciscosviana.stmservicos.api.controller;

import io.github.franciscosviana.stmservicos.common.client.model.ViaCepResponse;
import io.github.franciscosviana.stmservicos.domain.service.EnderecoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/enderecos")
public class EnderecoController {

    private final EnderecoService enderecoService;

    @GetMapping("/cep/{cep}")
    public ViaCepResponse buscar(@PathVariable String cep) {
        return enderecoService.consultarCep(cep);
    }
}
