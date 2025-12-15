package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.common.validation.ContratoException;
import io.github.franciscosviana.stmservicos.domain.model.Contrato;
import io.github.franciscosviana.stmservicos.domain.repository.ContratoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContratoService {

    private final ContratoRepository contratoRepository;

    public Contrato buscarOuFalhar(UUID id) {
        return contratoRepository.findById(id)
                .orElseThrow(() -> new ContratoException("Contrato não pertence ao cliente"));
    }
}
