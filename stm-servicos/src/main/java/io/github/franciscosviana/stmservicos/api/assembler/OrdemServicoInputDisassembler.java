package io.github.franciscosviana.stmservicos.api.assembler;

import io.github.franciscosviana.stmservicos.api.model.input.OrdemServicoInput;
import io.github.franciscosviana.stmservicos.domain.model.*;
import io.github.franciscosviana.stmservicos.domain.model.enums.StatusOrdem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class OrdemServicoInputDisassembler {

    public OrdemServico toDomainObject(OrdemServicoInput input) {
        if (input == null) return null;

        OrdemServico ordem = new OrdemServico();

        ordem.setOsClt(input.getOsClt());
        ordem.setOsg(input.getOsg());
        ordem.setDataHora(OffsetDateTime.now());

        if (input.getStatus() != null) {
            ordem.setStatus(StatusOrdem.valueOf(input.getStatus()));
        }

        if (input.getClienteId() != null) {
            ordem.setCliente(
                    Cliente.builder()
                            .id(input.getClienteId())
                            .build()
            );
        }

        if (input.getCredenciadoId() != null) {
            ordem.setCredenciado(
                    Credenciado.builder()
                            .id(input.getCredenciadoId())
                            .build()
            );
        }

        if (input.getContratoId() != null) {
            Contrato contrato = new Contrato();

            contrato.setId(input.getContratoId());
            ordem.setContrato(contrato);
        }
        ordem.setContato(input.getContato());
        ordem.setDepartamento(input.getDepartamento());
        ordem.setTelefone(input.getTelefone());

        if (input.getEndereco() != null) {
            ordem.setEndereco(Endereco.from(input.getEndereco()));
        }

        ordem.setAcionador(input.getAcionador());
        ordem.setEquipamento(input.getEquipamento());
        ordem.setSerie(input.getSerie());
        ordem.setPib(input.getPib());
        ordem.setDefeito(input.getDefeito());
        ordem.setRastreio(input.getRastreio());

        if (input.getSolucao() != null) {
            SolucaoOS solucao = SolucaoOS.from(input.getSolucao());
            solucao.setOrdemServico(ordem);
            ordem.setSolucao(solucao);
        }

        if (input.getFaturamento() != null) {
            FaturamentoOS faturamento = FaturamentoOS.from(input.getFaturamento());
            faturamento.setOrdemServico(ordem);
            ordem.setFaturamento(faturamento);
        }

        return ordem;
    }

    public void copyToDomainObject(OrdemServicoInput input, OrdemServico ordem) {
        OrdemServico nova = toDomainObject(input);

        ordem.setOsClt(nova.getOsClt());
        ordem.setOsg(nova.getOsg());
        ordem.setStatus(nova.getStatus());
        ordem.setDataHora(nova.getDataHora());
        ordem.setCliente(nova.getCliente());
        ordem.setCredenciado(nova.getCredenciado());
        ordem.setContrato(nova.getContrato());
        ordem.setContato(nova.getContato());
        ordem.setDepartamento(nova.getDepartamento());
        ordem.setTelefone(nova.getTelefone());
        ordem.setEndereco(nova.getEndereco());
        ordem.setAcionador(nova.getAcionador());
        ordem.setEquipamento(nova.getEquipamento());
        ordem.setSerie(nova.getSerie());
        ordem.setPib(nova.getPib());
        ordem.setDefeito(nova.getDefeito());
        ordem.setRastreio(nova.getRastreio());
        ordem.setSolucao(nova.getSolucao());
        ordem.setFaturamento(nova.getFaturamento());
    }
}
