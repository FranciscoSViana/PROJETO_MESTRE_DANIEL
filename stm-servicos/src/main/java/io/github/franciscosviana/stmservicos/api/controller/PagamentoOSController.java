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
     * POST   /ordens-servico/{id}/pagamento  → cria ou atualiza o pagamento da OS
     * <p>
     * Idempotente: se já existe um pagamento para a OS, sobrescreve com os novos dados.
     * O cálculo do valorTotal é sempre refeito pelo back-end.
     */
    @PostMapping
    public ResponseEntity<PagamentoOSOutput> registrar(
            @PathVariable UUID ordemServicoId,
            @Valid @RequestBody PagamentoOSInput input) {

        PagamentoOSOutput output = pagamentoOSService.registrar(ordemServicoId, input);
        return ResponseEntity.ok(output);
    }

    /**
     * GET    /ordens-servico/{id}/pagamento  → retorna o pagamento da OS
     */
    @GetMapping
    public ResponseEntity<PagamentoOSOutput> buscar(@PathVariable UUID ordemServicoId) {
        return pagamentoOSService
                .buscarPorOrdemServicoOpcional(ordemServicoId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build()); // 204 quando não existe ainda
    }
}