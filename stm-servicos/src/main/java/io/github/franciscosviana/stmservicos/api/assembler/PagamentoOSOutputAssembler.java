package io.github.franciscosviana.stmservicos.api.assembler;

import io.github.franciscosviana.stmservicos.api.model.output.PagamentoOSOutput;
import io.github.franciscosviana.stmservicos.domain.model.PagamentoOS;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PagamentoOSOutputAssembler {

    public PagamentoOSOutput toModel(PagamentoOS p) {
        return PagamentoOSOutput.builder()
                .id(p.getId())
                .osClt(p.getOsClt())
                .osg(p.getOsg())
                .cliente(p.getCliente())
                .contrato(p.getContrato())
                .valorChamado(p.getValorChamado())
                .km(p.getKm())
                .valorKm(p.getValorKm())
                .pedagio(p.getPedagio())
                .estacionamento(p.getEstacionamento())
                .outros(p.getOutros())
                .valorOutros(p.getValorOutros())
                .valorTotal(p.getValorTotal())
                .lote(p.getLote())
                .cpfNf(p.getCpfNf())
                .tipoPagamento(p.getTipoPagamento())
                .banco(p.getBanco())
                .urlComprovante(p.getUrlComprovante())
                .dataPagamento(p.getDataPagamento())
                .criadoEm(p.getCriadoEm())
                // ✅ pago = true sempre que o registro existir no banco
                .pago(p.isPago())  // ← usa o campo real, não mais p.getId() != null
                .build();
    }

    public List<PagamentoOSOutput> toCollectionModel(List<PagamentoOS> list) {
        return list.stream()
                .map(this::toModel)
                .toList();
    }
}