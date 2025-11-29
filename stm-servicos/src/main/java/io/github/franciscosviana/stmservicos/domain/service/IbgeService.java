package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.model.output.EstadoOutput;
import io.github.franciscosviana.stmservicos.api.model.output.MunicipioOutput;
import io.github.franciscosviana.stmservicos.common.client.BrasilAPIClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IbgeService {

    private final BrasilAPIClient brasilAPIClient;

    public List<EstadoOutput> listarEstados() {
        return brasilAPIClient.buscarEstados()
                .stream()
                .map(e -> new EstadoOutput(e.getNome(), e.getSigla()))
                .toList();
    }

    public List<MunicipioOutput> listarMunicipios(String uf) {
        return brasilAPIClient.buscarMunicipios(uf.toUpperCase())
                .stream()
                .map(m -> new MunicipioOutput(m.getNome(), m.getCodigo_ibge()))
                .toList();
    }
}
