package io.github.franciscosviana.stmservicos.api.controller;


import io.github.franciscosviana.stmservicos.api.model.input.OrdemServicoInput;
import io.github.franciscosviana.stmservicos.api.model.input.SolucaoOSInput;
import io.github.franciscosviana.stmservicos.api.model.output.HistoricoOrdemServicoOutput;
import io.github.franciscosviana.stmservicos.api.model.output.OrdemServicoOutput;
import io.github.franciscosviana.stmservicos.api.model.output.SolucaoOSOutput;
import io.github.franciscosviana.stmservicos.domain.service.HistoricoOrdemServicoService;
import io.github.franciscosviana.stmservicos.domain.service.OrdemServicoService;
import io.github.franciscosviana.stmservicos.domain.service.SolucaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ordens-servico")
public class OrdemServicoController {

    private final SolucaoService solucaoService;
    private final OrdemServicoService ordemServicoService;
    private final HistoricoOrdemServicoService historicoOrdemServicoService;


    @PostMapping
    public ResponseEntity<OrdemServicoOutput> criar(@RequestBody OrdemServicoInput ordemServicoInput) {

        log.info("Input {}", ordemServicoInput);
        OrdemServicoOutput ordemServicoOutput = ordemServicoService.salvar(ordemServicoInput);

        log.info("OrdemServicoOutput {}", ordemServicoOutput);

        return ResponseEntity.ok(ordemServicoOutput);
    }

    @GetMapping
    public Page<OrdemServicoOutput> listar(@PageableDefault(sort = "osg", direction = Sort.Direction.ASC) Pageable pageable) {
        return ordemServicoService.listar(pageable);
    }


    @GetMapping("/{id}")
    public ResponseEntity<OrdemServicoOutput> buscar(@PathVariable UUID id) {
        OrdemServicoOutput ordemServicoOutput = ordemServicoService.buscarPorId(id);

        return ResponseEntity.ok(ordemServicoOutput);
    }


    @PutMapping("/{id}")
    public ResponseEntity<OrdemServicoOutput> atualizar(@PathVariable UUID id, @RequestBody OrdemServicoInput input) {
        OrdemServicoOutput ordemServicoOutput = ordemServicoService.atualizar(id, input);

        return ResponseEntity.ok(ordemServicoOutput);
    }

    @PostMapping("/{ordemId}/solucao")
    public ResponseEntity<SolucaoOSOutput> finalizar(
            @PathVariable UUID ordemId,
            @RequestBody SolucaoOSInput input) {

        log.info("Finalizando OS {}", ordemId);

        SolucaoOSOutput output = solucaoService.finalizarOS(ordemId, input);

        return ResponseEntity.ok(output);
    }

    @GetMapping("/{id}/historico")
    public List<HistoricoOrdemServicoOutput> historico(@PathVariable UUID id) {

        return historicoOrdemServicoService.listarPorOrdemServico(id);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable UUID id) {
        ordemServicoService.deletar(id);
    }

    @GetMapping("/proximo-osg")
    public String proximoOsg() {
        return ordemServicoService.gerarProximoOsg();
    }
}
