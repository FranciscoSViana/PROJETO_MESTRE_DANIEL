package io.github.franciscosviana.stmservicos.api.controller;

import io.github.franciscosviana.stmservicos.api.model.input.ClienteInput;
import io.github.franciscosviana.stmservicos.api.model.output.ClienteOutput;
import io.github.franciscosviana.stmservicos.common.client.model.ReceitaWsResponse;
import io.github.franciscosviana.stmservicos.domain.service.ClienteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    public ResponseEntity<ClienteOutput> salvar(@RequestBody ClienteInput clienteInput){
        ClienteOutput clienteOutput = clienteService.salvar(clienteInput);

        return ResponseEntity.ok(clienteOutput);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteOutput> buscarPorId(@PathVariable UUID id) {

        ClienteOutput cliente = clienteService.buscarPorId(id);

        return ResponseEntity.ok(cliente);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {

        clienteService.excluir(id);

        return ResponseEntity.noContent().build();
    }


    @GetMapping
    public Page<ClienteOutput> listar(@PageableDefault Pageable pageable){

        return clienteService.listar(pageable);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteOutput> atualizar(@PathVariable UUID id,
                                                   @RequestBody ClienteInput clienteInput) {

        ClienteOutput clienteOutput = clienteService.atualizar(id, clienteInput);
        return ResponseEntity.ok(clienteOutput);
    }


    @GetMapping("/cnpj/{cnpj}")
    public ResponseEntity<ReceitaWsResponse> consultarCnpj(@PathVariable String cnpj){
        ReceitaWsResponse receitaWsResponse = clienteService.consultarCnpj(cnpj);

        Map<String, Object> mapa = new HashMap<>();
        mapa.put("razaoSocial", receitaWsResponse.getRazaoSocial());
        mapa.put("nomeFantasia", receitaWsResponse.getNomeFantasia());
        mapa.put("situacaoCadastral", receitaWsResponse.getSituacaoCadastral());
        mapa.put("cnpj", cnpj);

        log.info("CNPJ: {}", receitaWsResponse.getRazaoSocial());

        return ResponseEntity.ok(receitaWsResponse);
    }
}
