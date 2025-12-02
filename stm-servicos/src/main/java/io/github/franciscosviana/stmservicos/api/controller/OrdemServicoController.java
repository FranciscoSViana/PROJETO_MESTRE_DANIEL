package io.github.franciscosviana.stmservicos.api.controller;


import io.github.franciscosviana.stmservicos.api.model.input.OrdemServicoInput;
import io.github.franciscosviana.stmservicos.api.model.output.OrdemServicoOutput;
import io.github.franciscosviana.stmservicos.domain.model.OrdemServico;
import io.github.franciscosviana.stmservicos.domain.service.OrdemServicoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ordens-servico")
public class OrdemServicoController {


    private final OrdemServicoService ordemServicoService;


    @PostMapping
    public ResponseEntity<OrdemServicoOutput> criar(@RequestBody OrdemServicoInput ordemServicoInput) {

        log.info("Input {}", ordemServicoInput);
        OrdemServicoOutput ordemServicoOutput = ordemServicoService.salvar(ordemServicoInput);

        return ResponseEntity.ok(ordemServicoOutput);
    }

    @GetMapping
    public Page<OrdemServicoOutput> listar(@PageableDefault Pageable pageable) {
        return ordemServicoService.listar(pageable);
    }


    @GetMapping("/{id}")
    public OrdemServicoOutput buscar(@PathVariable Long id) {
        OrdemServico ordemServico = ordemServicoService.buscarPorId(id);

        return ordemServicoService.converterOrdem(ordemServico);
    }


    @PutMapping("/{id}")
    public ResponseEntity<OrdemServicoOutput> atualizar(@PathVariable Long id, @RequestBody OrdemServicoInput input) {
        OrdemServicoOutput ordemServicoOutput = ordemServicoService.atualizar(id, input);

        return ResponseEntity.ok(ordemServicoOutput);
    }

    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id) {
        ordemServicoService.deletar(id);
    }

    @GetMapping("/proximo-osg")
    public String proximoOsg() {
        return ordemServicoService.gerarProximoOsg();
    }
}
