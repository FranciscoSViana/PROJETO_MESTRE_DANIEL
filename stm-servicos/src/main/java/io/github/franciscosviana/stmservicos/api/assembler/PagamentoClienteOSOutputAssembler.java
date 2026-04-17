package io.github.franciscosviana.stmservicos.api.assembler;

import io.github.franciscosviana.stmservicos.api.model.output.PagamentoClienteOSOutput;
import io.github.franciscosviana.stmservicos.domain.model.PagamentoClienteOS;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PagamentoClienteOSOutputAssembler {

    public PagamentoClienteOSOutput toModel(PagamentoClienteOS p) {
        return PagamentoClienteOSOutput.builder()
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
                .nf(p.getNf())
                .tipoPagamento(p.getTipoPagamento())
                .banco(p.getBanco())
                .urlComprovante(p.getUrlComprovante())
                .dataPrevista(p.getDataPrevista())
                .dataPagamento(p.getDataPagamento())
                .criadoEm(p.getCriadoEm())
                .recebido(p.isRecebido())
                .build();
    }

    public List<PagamentoClienteOSOutput> toCollectionModel(List<PagamentoClienteOS> list) {
        return list.stream().map(this::toModel).toList();
    }
}