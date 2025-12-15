package io.github.franciscosviana.stmservicos.api.controller;

import io.github.franciscosviana.stmservicos.api.model.input.TecnicoInput;
import io.github.franciscosviana.stmservicos.api.model.output.TecnicoOutput;
import io.github.franciscosviana.stmservicos.domain.service.TecnicoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/credenciados")
public class CredenciadoTecnicoController {

    private final TecnicoService tecnicoService;

    @PostMapping("/{credenciadoId}/tecnicos")
    public ResponseEntity<TecnicoOutput> adicionarTecnico(
            @PathVariable UUID credenciadoId,
            @RequestBody TecnicoInput input) {

        return ResponseEntity.ok(
                tecnicoService.salvar(credenciadoId, input)
        );
    }

    @GetMapping("/{credenciadoId}/tecnicos")
    public Page<TecnicoOutput> listarTecnicos(@PathVariable UUID credenciadoId, @PageableDefault Pageable pageable) {
        return tecnicoService.listarPorCredenciado(credenciadoId,  pageable);
    }

    @GetMapping("/tecnicos/{tecnicoId}")
    public TecnicoOutput buscarTecnico(@PathVariable UUID tecnicoId) {
        return tecnicoService.buscarPorId(tecnicoId);
    }

    @PutMapping("/tecnicos/{tecnicoId}")
    public TecnicoOutput atualizarTecnico(
            @PathVariable UUID tecnicoId,
            @RequestBody TecnicoInput input) {

        return tecnicoService.atualizar(tecnicoId, input);
    }

    @DeleteMapping("/tecnicos/{tecnicoId}")
    public ResponseEntity<Void> excluirTecnico(@PathVariable UUID tecnicoId) {
        tecnicoService.excluir(tecnicoId);
        return ResponseEntity.noContent().build();
    }
}

