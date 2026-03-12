package io.github.franciscosviana.stmservicos.api.controller;


import io.github.franciscosviana.stmservicos.api.model.input.OrdemServicoInput;
import io.github.franciscosviana.stmservicos.api.model.input.SolucaoOSInput;
import io.github.franciscosviana.stmservicos.api.model.output.HistoricoOrdemServicoOutput;
import io.github.franciscosviana.stmservicos.api.model.output.OrdemServicoOutput;
import io.github.franciscosviana.stmservicos.api.model.output.SolucaoOSOutput;
import io.github.franciscosviana.stmservicos.domain.service.HistoricoOrdemServicoService;
import io.github.franciscosviana.stmservicos.domain.service.OrdemServicoExportService;
import io.github.franciscosviana.stmservicos.domain.service.OrdemServicoService;
import io.github.franciscosviana.stmservicos.domain.service.SolucaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ordens-servico")
public class OrdemServicoController {

    private final SolucaoService solucaoService;
    private final OrdemServicoService ordemServicoService;
    private final OrdemServicoExportService exportService;
    private final HistoricoOrdemServicoService historicoOrdemServicoService;


    @PostMapping
    public ResponseEntity<OrdemServicoOutput> criar(@RequestBody OrdemServicoInput ordemServicoInput) {

        log.info("Input {}", ordemServicoInput);
        OrdemServicoOutput ordemServicoOutput = ordemServicoService.salvar(ordemServicoInput);

        log.info("OrdemServicoOutput {}", ordemServicoOutput);

        return ResponseEntity.ok(ordemServicoOutput);
    }

    @GetMapping
    public Page<OrdemServicoOutput> listar(
            @RequestParam(required = false) String osClt,
            @RequestParam(required = false) String osg,
            @RequestParam(required = false) String dataAbertura,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) String credenciado,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String rastreio,
            Pageable pageable
    ) {

        return ordemServicoService.listar(
                pageable, osClt, osg, dataAbertura, status,
                cliente, credenciado,
                cidade, estado, rastreio
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdemServicoOutput> buscar(@PathVariable UUID id) {
        OrdemServicoOutput ordemServicoOutput = ordemServicoService.buscarPorId(id);

        return ResponseEntity.ok(ordemServicoOutput);
    }

    // OrdemServicoController.java
    @PatchMapping("/{id}/rastreio")
    public ResponseEntity<OrdemServicoOutput> atualizarStatusRastreio(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {

        String statusRastreio = body.get("statusRastreio");
        OrdemServicoOutput output = ordemServicoService.atualizarStatusRastreio(id, statusRastreio);
        return ResponseEntity.ok(output);
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

    // ─── XLSX ────────────────────────────────────────────────────────────
    @GetMapping("/exportar/xlsx")
    public ResponseEntity<byte[]> exportarXlsx(
            @RequestParam(required = false) String osClt,
            @RequestParam(required = false) String osg,
            @RequestParam(required = false) String dataAbertura,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) String credenciado,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String rastreio
    ) {
        byte[] bytes = exportService.exportarXlsx(
                osClt, osg, dataAbertura, status, cliente, credenciado, cidade, estado, rastreio);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"ordens-servico.xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    // ─── CSV ─────────────────────────────────────────────────────────────
    @GetMapping("/exportar/csv")
    public ResponseEntity<byte[]> exportarCsv(
            @RequestParam(required = false) String osClt,
            @RequestParam(required = false) String osg,
            @RequestParam(required = false) String dataAbertura,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) String credenciado,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String rastreio
    ) {
        byte[] bytes = exportService.exportarCsv(
                osClt, osg, dataAbertura, status, cliente, credenciado, cidade, estado, rastreio);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"ordens-servico.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }

    // ─── PDF ─────────────────────────────────────────────────────────────
    @GetMapping("/exportar/pdf")
    public ResponseEntity<byte[]> exportarPdf(
            @RequestParam(required = false) String osClt,
            @RequestParam(required = false) String osg,
            @RequestParam(required = false) String dataAbertura,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) String credenciado,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String rastreio
    ) {
        byte[] bytes = exportService.exportarPdf(
                osClt, osg, dataAbertura, status, cliente, credenciado, cidade, estado, rastreio);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"ordens-servico.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }
}
