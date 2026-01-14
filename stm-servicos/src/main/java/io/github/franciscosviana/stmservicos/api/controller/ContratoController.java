package io.github.franciscosviana.stmservicos.api.controller;

import io.github.franciscosviana.stmservicos.api.model.input.ContratoInput;
import io.github.franciscosviana.stmservicos.api.model.output.ContratoOutput;
import io.github.franciscosviana.stmservicos.domain.service.ContratoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clientes/{clienteId}/contratos")
public class ContratoController {

    private final ContratoService contratoService;

    @PostMapping
    public ResponseEntity<ContratoOutput> adicionar(
            @PathVariable UUID clienteId,
            @RequestBody ContratoInput input) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(contratoService.adicionar(clienteId, input));
    }

    @GetMapping
    public List<ContratoOutput> listar(@PathVariable UUID clienteId) {
        return contratoService.listar(clienteId);
    }

    @PutMapping("/{contratoId}")
    public ContratoOutput atualizar(
            @PathVariable UUID clienteId,
            @PathVariable UUID contratoId,
            @RequestBody ContratoInput input) {

        return contratoService.atualizar(clienteId, contratoId, input);
    }

    @DeleteMapping("/{contratoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(
            @PathVariable UUID clienteId,
            @PathVariable UUID contratoId) {

        contratoService.excluir(clienteId, contratoId);
    }
}

