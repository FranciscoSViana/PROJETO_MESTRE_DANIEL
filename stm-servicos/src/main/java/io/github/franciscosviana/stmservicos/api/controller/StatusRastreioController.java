package io.github.franciscosviana.stmservicos.api.controller;

import io.github.franciscosviana.stmservicos.domain.model.enums.StatusRastreio;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rastreio/status")
public class StatusRastreioController {

    @GetMapping
    public List<Map<String, String>> listar() {
        return Arrays.stream(StatusRastreio.values())
                .map(s -> Map.of(
                        "value", s.name(),
                        "descricao", s.getDescricao(),
                        "cor", s.getCor()
                ))
                .toList();
    }
}
