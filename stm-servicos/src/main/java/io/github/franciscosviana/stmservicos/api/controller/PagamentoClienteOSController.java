package io.github.franciscosviana.stmservicos.api.controller;

import io.github.franciscosviana.stmservicos.api.model.input.PagamentoClienteOSInput;
import io.github.franciscosviana.stmservicos.api.model.output.PagamentoClienteOSOutput;
import io.github.franciscosviana.stmservicos.domain.service.PagamentoClienteOSService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ordens-servico/{ordemServicoId}/recebimento")
public class PagamentoClienteOSController {

    private final PagamentoClienteOSService pagamentoClienteOSService;

    /**
     * POST → registra o recebimento (só se ainda não foi recebido)
     */
    @PostMapping
    public ResponseEntity<PagamentoClienteOSOutput> registrar(
            @PathVariable UUID ordemServicoId,
            @Valid @RequestBody PagamentoClienteOSInput input) {

        PagamentoClienteOSOutput output = pagamentoClienteOSService.registrar(ordemServicoId, input);
        return ResponseEntity.ok(output);
    }

    /**
     * PUT → edita um recebimento já existente
     */
    @PutMapping
    public ResponseEntity<PagamentoClienteOSOutput> editar(
            @PathVariable UUID ordemServicoId,
            @Valid @RequestBody PagamentoClienteOSInput input) {

        PagamentoClienteOSOutput output = pagamentoClienteOSService.editar(ordemServicoId, input);
        return ResponseEntity.ok(output);
    }

    /**
     * GET → retorna o recebimento da OS (204 se ainda não existe)
     */
    @GetMapping
    public ResponseEntity<PagamentoClienteOSOutput> buscar(@PathVariable UUID ordemServicoId) {
        return pagamentoClienteOSService
                .buscarPorOrdemServicoOpcional(ordemServicoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}