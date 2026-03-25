package io.github.franciscosviana.stmservicos.api.controller;

import io.github.franciscosviana.stmservicos.api.model.input.PagamentoOSInput;
import io.github.franciscosviana.stmservicos.api.model.output.PagamentoOSOutput;
import io.github.franciscosviana.stmservicos.domain.service.PagamentoOSService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ordens-servico/{ordemServicoId}/pagamento")
public class PagamentoOSController {

    private final PagamentoOSService pagamentoOSService;

    /**
     * POST → cria o pagamento (só se ainda não foi pago)
     */
    @PostMapping
    public ResponseEntity<PagamentoOSOutput> registrar(
            @PathVariable UUID ordemServicoId,
            @Valid @RequestBody PagamentoOSInput input) {

        PagamentoOSOutput output = pagamentoOSService.registrar(ordemServicoId, input);
        return ResponseEntity.ok(output);
    }

    /**
     * PUT → edita um pagamento já existente
     */
    @PutMapping
    public ResponseEntity<PagamentoOSOutput> editar(
            @PathVariable UUID ordemServicoId,
            @Valid @RequestBody PagamentoOSInput input) {

        PagamentoOSOutput output = pagamentoOSService.editar(ordemServicoId, input);
        return ResponseEntity.ok(output);
    }

    /**
     * GET → retorna o pagamento da OS (204 se ainda não existe)
     */
    @GetMapping
    public ResponseEntity<PagamentoOSOutput> buscar(@PathVariable UUID ordemServicoId) {
        return pagamentoOSService
                .buscarPorOrdemServicoOpcional(ordemServicoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}