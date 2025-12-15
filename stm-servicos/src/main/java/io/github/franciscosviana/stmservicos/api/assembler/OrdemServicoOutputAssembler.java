package io.github.franciscosviana.stmservicos.api.assembler;

import io.github.franciscosviana.stmservicos.api.model.output.*;
import io.github.franciscosviana.stmservicos.domain.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrdemServicoOutputAssembler {

    private final CredenciadoOutputAssembler credenciadoAssembler;
    private final ContratoOutputAssembler contratoOutputAssembler;

    public OrdemServicoOutput toModel(OrdemServico os) {
        if (os == null) return null;

        return OrdemServicoOutput.builder()
                .id(os.getId())
                .osClt(os.getOsClt())
                .osg(os.getOsg())
                .status(os.getStatus() != null ? os.getStatus().name() : null)
                .dataHora(os.getDataHora())
                .cliente(toCliente(os.getCliente()))
                .credenciado(os.getCredenciado() != null
                        ? credenciadoAssembler.toModel(os.getCredenciado())
                        : null)
                .contrato(os.getContrato() != null
                        ? contratoOutputAssembler.toModel(os.getContrato())
                        : null)
                .contato(os.getContato())
                .departamento(os.getDepartamento())
                .telefone(os.getTelefone())
                .endereco(toEndereco(os.getEndereco()))
                .acionador(os.getAcionador())
                .equipamento(os.getEquipamento())
                .serie(os.getSerie())
                .pib(os.getPib())
                .defeito(os.getDefeito())
                .rastreio(os.getRastreio())
                .solucao(toSolucao(os.getSolucao()))
                .faturamento(toFaturamento(os.getFaturamento()))
                .build();
    }

    private ClienteOutput toCliente(Cliente cliente) {
        if (cliente == null) return null;

        return ClienteOutput.builder()
                .id(cliente.getId())
                .codigo(cliente.getCodigo())
                .nome(cliente.getNome())
                .build();

    }

    private EnderecoOutput toEndereco(Endereco e) {
        if (e == null) return null;

        return EnderecoOutput.builder()
                .cep(e.getCep())
                .logradouro(e.getLogradouro())
                .bairro(e.getBairro())
                .numero(e.getNumero())
                .complemento(e.getComplemento())
                .build();
    }

    private SolucaoOSOutput toSolucao(SolucaoOS s) {
        if (s == null) return null;

        return SolucaoOSOutput.builder()
                .id(s.getId())
                .tecnico(s.getTecnico())
                .dataVisita(s.getDataVisita())
                .inicio(s.getInicio())
                .termino(s.getTermino())
                .solucao(s.getSolucao())
                .km(s.getKm())
                .pedagios(s.getPedagios())
                .estac(s.getEstac())
                .outros(s.getOutros())
                .build();
    }

    private FaturamentoOSOutput toFaturamento(FaturamentoOS f) {
        if (f == null) return null;

        return FaturamentoOSOutput.builder()
                .id(f.getId())
                .cliente(f.getCliente())
                .contrato(f.getContrato())
                .statusOs(f.getStatusOs())
                .rag(f.getRag())
                .chamado(f.getChamado())
                .deslocamento(f.getDeslocamento())
                .ttlKm(f.getTtlKm())
                .pedagios(f.getPedagios())
                .estac(f.getEstac())
                .outros(f.getOutros())
                .total(f.getTotal())
                .doc(f.getDoc())
                .status(f.getStatus())
                .km(f.getKm())
                .totalGeral(f.getTotalGeral())
                .notaFiscal(f.getNotaFiscal())
                .faturado(f.getFaturado())
                .saldoMO(f.getSaldoMO())
                .saldoKm(f.getSaldoKm())
                .saldoOutros(f.getSaldoOutros())
                .imposto(f.getImposto())
                .saldoTotal(f.getSaldoTotal())
                .build();
    }
}
