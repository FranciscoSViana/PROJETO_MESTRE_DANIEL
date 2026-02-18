package io.github.franciscosviana.stmservicos.api.controller;

import io.github.franciscosviana.stmservicos.api.model.output.SolucaoOSOutput;
import io.github.franciscosviana.stmservicos.domain.service.SolucaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/solucoes")
public class SolucaoController {

    private final SolucaoService solucaoService;

    @GetMapping
    public Page<SolucaoOSOutput> listarTodas(@PageableDefault Pageable pageable) {

        log.info("Listando todas as soluções");

        return solucaoService.listarTodas(pageable);
    }

    @GetMapping("/{id}")
    public SolucaoOSOutput buscarPorId(@PathVariable Long id) {

        log.info("Buscando solução {}", id);

        return solucaoService.buscarPorId(id);
    }
}
