package io.github.franciscosviana.stmservicos.api.controller;

import io.github.franciscosviana.stmservicos.api.model.input.CredenciadoInput;
import io.github.franciscosviana.stmservicos.api.model.output.CredenciadoOutput;
import io.github.franciscosviana.stmservicos.common.client.model.EstadoResponse;
import io.github.franciscosviana.stmservicos.common.client.model.MunicipioResponse;
import io.github.franciscosviana.stmservicos.domain.service.CredenciadoService;
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
public class CredenciadoController {

    private final CredenciadoService credenciadoService;

    @PostMapping
    public ResponseEntity<CredenciadoOutput> salvar(@RequestBody CredenciadoInput credenciadoInput) {
        CredenciadoOutput credenciadoOutput = credenciadoService.salvar(credenciadoInput);

        return ResponseEntity.ok(credenciadoOutput);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CredenciadoOutput> buscarPorId(@PathVariable UUID id) {

        CredenciadoOutput credenciadoOutput = credenciadoService.buscarPorId(id);

        return ResponseEntity.ok(credenciadoOutput);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {

        credenciadoService.excluir(id);

        return ResponseEntity.noContent().build();
    }


    @GetMapping
    public Page<CredenciadoOutput> listar(@PageableDefault Pageable pageable) {

        return credenciadoService.listar(pageable);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CredenciadoOutput> atualizar(@PathVariable UUID id,
                                                       @RequestBody CredenciadoInput credenciadoInput) {

        CredenciadoOutput credenciadoOutput = credenciadoService.atualizar(id, credenciadoInput);
        return ResponseEntity.ok(credenciadoOutput);
    }

    // Endpoint para listar todos os estados
    @GetMapping("/estados")
    public ResponseEntity<List<EstadoResponse>> listarEstados() {
        List<EstadoResponse> estados = credenciadoService.listarEstados();
        return ResponseEntity.ok(estados);
    }

    // Endpoint para listar municípios de uma UF
    @GetMapping("/municipios/{uf}")
    public ResponseEntity<List<MunicipioResponse>> listarMunicipios(@PathVariable String uf) {
        List<MunicipioResponse> municipios = credenciadoService.listarMunicipios(uf);
        return ResponseEntity.ok(municipios);
    }

}
