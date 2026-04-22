package io.github.franciscosviana.stmservicos.api.controller;

import io.github.franciscosviana.stmservicos.api.model.input.ContasReceberFilter;
import io.github.franciscosviana.stmservicos.api.model.input.PagamentoLoteInput;
import io.github.franciscosviana.stmservicos.api.model.output.ContasReceberOutput;
import io.github.franciscosviana.stmservicos.api.model.output.ContasReceberTotaisOutput;
import io.github.franciscosviana.stmservicos.api.model.output.PagamentoLoteResultado;
import io.github.franciscosviana.stmservicos.domain.service.ContasReceberService;
import io.github.franciscosviana.stmservicos.domain.service.PagamentoClienteOSService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/financeiro/contas-receber")
public class ContasReceberController {

    private final ContasReceberService contasReceberService;
    private final PagamentoClienteOSService pagamentoClienteOSService;

    @GetMapping
    public Page<ContasReceberOutput> listar(@ModelAttribute ContasReceberFilter filtro,
                                            Pageable pageable) {
        return contasReceberService.listar(filtro, pageable);
    }

    @GetMapping("/totais")
    public ContasReceberTotaisOutput totais(@ModelAttribute ContasReceberFilter filtro) {
        return contasReceberService.buscarTotais(filtro);
    }

    @GetMapping("/lotes")
    public List<String> listarLotes() {
        return contasReceberService.listarLotes();
    }

    // ── Lotes por cliente ────────────────────────────────────────────────────────
    @GetMapping("/lotes-por-cliente")
    public List<String> listarLotesPorCliente(@RequestParam String cliente) {
        return contasReceberService.listarLotesPorCliente(cliente);
    }

    // ── OS pendentes por cliente + lote ─────────────────────────────────────────
    @GetMapping("/os-pendentes")
    public List<ContasReceberOutput> listarOsPendentes(
            @RequestParam String cliente,
            @RequestParam String lote) {
        return contasReceberService.listarOsPendentes(cliente, lote);
    }

    // ── Pagamento em lote ────────────────────────────────────────────────────────
    @PostMapping("/pagamento-lote")
    public ResponseEntity<PagamentoLoteResultado> registrarPagamentoLote(
            @RequestBody PagamentoLoteInput input) {
        PagamentoLoteResultado resultado = pagamentoClienteOSService.registrarLote(input);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/exportar/xlsx")
    public ResponseEntity<byte[]> exportarXlsx(@ModelAttribute ContasReceberFilter filtro) {
        byte[] bytes = contasReceberService.exportarXlsx(filtro);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"contas-receber.xlsx\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    @GetMapping("/exportar/pdf")
    public ResponseEntity<byte[]> exportarPdf(@ModelAttribute ContasReceberFilter filtro) {
        byte[] bytes = contasReceberService.exportarPdf(filtro);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"contas-receber.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }
}