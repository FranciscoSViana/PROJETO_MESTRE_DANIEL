package io.github.franciscosviana.stmservicos.api.controller;

import io.github.franciscosviana.stmservicos.api.model.input.ContasPagarFilter;
import io.github.franciscosviana.stmservicos.api.model.output.ContasPagarOutput;
import io.github.franciscosviana.stmservicos.domain.service.ContasPagarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/financeiro/contas-pagar")
public class ContasPagarController {

    private final ContasPagarService contasPagarService;

    @GetMapping
    public Page<ContasPagarOutput> listar(
            @ModelAttribute ContasPagarFilter filtro,
            Pageable pageable) {
        log.info(">>> contasPagar filtro={} pageable={}", filtro, pageable);
        Page<ContasPagarOutput> result = contasPagarService.listar(filtro, pageable);
        log.info(">>> total={}", result.getTotalElements());
        return result;
    }

    @GetMapping("/exportar/xlsx")
    public ResponseEntity<byte[]> exportarXlsx(@ModelAttribute ContasPagarFilter filtro) {
        byte[] bytes = contasPagarService.exportarXlsx(filtro);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"contas-pagar.xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @GetMapping("/exportar/pdf")
    public ResponseEntity<byte[]> exportarPdf(@ModelAttribute ContasPagarFilter filtro) {
        byte[] bytes = contasPagarService.exportarPdf(filtro);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"contas-pagar.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }
}