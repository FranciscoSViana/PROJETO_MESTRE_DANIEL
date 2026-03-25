package io.github.franciscosviana.stmservicos.api.assembler;

import io.github.franciscosviana.stmservicos.api.model.input.PagamentoOSInput;
import io.github.franciscosviana.stmservicos.domain.model.PagamentoOS;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PagamentoOSInputDisassembler {

    private final ModelMapper modelMapper;

    public PagamentoOS toDomainObject(PagamentoOSInput input) {
        return modelMapper.map(input, PagamentoOS.class);
    }

    public void copyToDomainObject(PagamentoOSInput input, PagamentoOS pagamento) {
        modelMapper.map(input, pagamento);
    }
}