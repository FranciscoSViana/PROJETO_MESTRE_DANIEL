package io.github.franciscosviana.stmservicos.api.controller;

import io.github.franciscosviana.stmservicos.api.model.output.EstadoOutput;
import io.github.franciscosviana.stmservicos.api.model.output.MunicipioOutput;
import io.github.franciscosviana.stmservicos.domain.service.IbgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ibge")
@CrossOrigin(origins = "http://localhost:4200")
public class IbgeController {

    private final IbgeService ibgeService;

    @GetMapping("/estados")
    public List<EstadoOutput> listarEstados(){
        return ibgeService.listarEstados();
    }

    @GetMapping("/estados/{uf}/municipios")
    public List<MunicipioOutput>  listarMunicipios(@PathVariable String uf){
        return ibgeService.listarMunicipios(uf);
    }
}
