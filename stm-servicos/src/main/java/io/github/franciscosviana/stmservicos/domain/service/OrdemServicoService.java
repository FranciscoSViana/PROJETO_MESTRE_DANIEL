package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.model.input.OrdemServicoInput;
import io.github.franciscosviana.stmservicos.api.model.output.*;
import io.github.franciscosviana.stmservicos.domain.model.*;
import io.github.franciscosviana.stmservicos.domain.repository.OrdemServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class OrdemServicoService {

    private final OrdemServicoRepository ordemServicoRepository;


    public OrdemServicoOutput salvar(OrdemServicoInput ordemServicoInput) {
        OrdemServico ordemServico = OrdemServico.from(ordemServicoInput);
        ordemServicoRepository.save(ordemServico);

        return converterOrdem(ordemServico);
    }


    public OrdemServico buscarPorId(Long id) {
        return ordemServicoRepository.findById(id).orElseThrow(() ->
                new RuntimeException("Ordem de serviço não encontrada"));
    }


    public Page<OrdemServicoOutput> listar(Pageable pageable) {
        Page<OrdemServico> ordemServicos = ordemServicoRepository.findAll(pageable);

        return ordemServicos.map(this::converterOrdem);
    }


    public OrdemServicoOutput atualizar(Long id, OrdemServicoInput input) {
        OrdemServico ordemAtual = ordemServicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordem de Serviço não encontrada"));

        // -------- CAMPOS SIMPLES --------
        ordemAtual.setOsClt(input.getOsClt());
        ordemAtual.setOsg(input.getOsg());
        ordemAtual.setStatus(input.getStatus());
        ordemAtual.setRag(input.getRag());
        ordemAtual.setDataHora(input.getDataHora());
        ordemAtual.setContrato(input.getContrato());
        ordemAtual.setContato(input.getContato());
        ordemAtual.setDepartamento(input.getDepartamento());
        ordemAtual.setTelefone(input.getTelefone());
        ordemAtual.setAcionador(input.getAcionador());
        ordemAtual.setEquipamento(input.getEquipamento());
        ordemAtual.setSerie(input.getSerie());
        ordemAtual.setPib(input.getPib());
        ordemAtual.setDefeito(input.getDefeito());
        ordemAtual.setRastreio(input.getRastreio());

        // -------- CLIENTE --------
        if (input.getClienteId() != null) {
            ordemAtual.setCliente(
                    Cliente.builder()
                            .id(input.getClienteId())
                            .build()
            );
        } else {
            ordemAtual.setCliente(null);
        }

        // -------- CREDENCIADO --------
        if (input.getCredenciadoId() != null) {
            ordemAtual.setCredenciado(
                    Credenciado.builder()
                            .id(input.getCredenciadoId())
                            .build()
            );
        } else {
            ordemAtual.setCredenciado(null);
        }

        // -------- ENDEREÇO EMBUTIDO --------
        ordemAtual.setEndereco(
                Endereco.from(input.getEndereco())
        );

        // -------- SOLUÇÃO OS --------
        if (input.getSolucao() != null) {
            SolucaoOS solucao = SolucaoOS.from(input.getSolucao());
            solucao.setOrdemServico(ordemAtual);
            ordemAtual.setSolucao(solucao);
        } else {
            ordemAtual.setSolucao(null);
        }

        // -------- FATURAMENTO --------
        if (input.getFaturamento() != null) {
            FaturamentoOS faturamento = FaturamentoOS.from(input.getFaturamento());
            faturamento.setOrdemServico(ordemAtual);
            ordemAtual.setFaturamento(faturamento);
        } else {
            ordemAtual.setFaturamento(null);
        }

        // -------- SALVA --------
        ordemServicoRepository.save(ordemAtual);

        return converterOrdem(ordemAtual);
    }


    public void deletar(Long id) {
        ordemServicoRepository.deleteById(id);
    }

    public OrdemServicoOutput converterOrdem(OrdemServico os) {
        if (os == null) return null;

        return OrdemServicoOutput.builder()
                .id(os.getId())
                .osClt(os.getOsClt())
                .osg(os.getOsg())
                .status(os.getStatus())
                .rag(os.getRag())
                .dataHora(os.getDataHora())
                .cliente(converterCliente(os.getCliente()))
                .credenciado(converterCredenciado(os.getCredenciado()))
                .contrato(os.getContrato())
                .contato(os.getContato())
                .departamento(os.getDepartamento())
                .telefone(os.getTelefone())
                .endereco(converterEndereco(os.getEndereco()))
                .acionador(os.getAcionador())
                .equipamento(os.getEquipamento())
                .serie(os.getSerie())
                .pib(os.getPib())
                .defeito(os.getDefeito())
                .rastreio(os.getRastreio())
                .solucao(converterSolucao(os.getSolucao()))
                .faturamento(converterFaturamento(os.getFaturamento()))
                .build();
    }

    public String gerarProximoOsg() {
        String anoAtual = String.valueOf(OffsetDateTime.now().getYear());

        // Busca o último OSG cadastrado
        String ultimoOsg = ordemServicoRepository.findLastOsg(); // pode ser null

        // Se não existe OSG no banco, devolve a primeira do ano
        if (ultimoOsg == null) {
            return "OSG" + anoAtual + "0001";
        }

        // extrai o ano da última OSG
        String anoUltimo = ultimoOsg.substring(3, 7);

        // extrai a parte numérica (independente do tamanho)
        String seqStr = ultimoOsg.substring(7);
        int sequencia = Integer.parseInt(seqStr);

        // Se mudou o ano → reinicia sequência
        if (!anoUltimo.equals(anoAtual)) {
            return "OSG" + anoAtual + "0001";
        }

        // incrementa sempre
        sequencia++;

        // nova sequência SEM limite e SEM fixar tamanho,
        // mas mantendo zeros somente enquanto <= 9999
        String novaSequencia;

        // se ainda tiver menos que 4 dígitos, mantém zeros
        if (sequencia <= 9999) {
            novaSequencia = String.format("%04d", sequencia);
        } else {
            // passou de 9999 → usa tamanho natural (sem preencher)
            novaSequencia = String.valueOf(sequencia);
        }

        return "OSG" + anoAtual + novaSequencia;
    }

    public ClienteOutput converterCliente(Cliente cliente) {
        if (cliente == null) return null;
        return ClienteOutput.builder()
                .id(cliente.getId())
                .codigo(cliente.getCodigo())
                .contrato(cliente.getContrato())
                .nome(cliente.getNome())
                .valorChamado(cliente.getValorChamado())
                .valorKm(cliente.getValorKm())
                .cnpj(cliente.getCnpj())
                .inscricaoEstadual(cliente.getInscricaoEstadual())
                .razaoSocial(cliente.getRazaoSocial())
                .build();
    }

    public CredenciadoOutput converterCredenciado(Credenciado credenciado) {
        if (credenciado == null) return null;

        return getCredenciadoOutput(credenciado);
    }

    static CredenciadoOutput getCredenciadoOutput(Credenciado credenciado) {
        return CredenciadoOutput.builder()
                .id(credenciado.getId())
                .codigo(credenciado.getCodigo())
                .rag(credenciado.getRag())
                .cidade(credenciado.getCidade())
                .uf(credenciado.getUf())
                .tipo(credenciado.getTipo())
                .valorChamado(credenciado.getValorChamado())
                .quantidadeOSAtendidas(credenciado.getQuantidadeOSAtendidas())
                .contato(credenciado.getContato())
                .telefones(credenciado.getTelefones())
                .email(credenciado.getEmail())
                .tecnico(credenciado.getTecnico())
                .cpf(credenciado.getCpf())
                .base(credenciado.getBase())
                .build();
    }

    public EnderecoOutput converterEndereco(Endereco e) {
        return EnderecoOutput.builder()
                .cep(e.getCep())
                .logradouro(e.getLogradouro())
                .bairro(e.getBairro())
                .numero(e.getNumero())
                .complemento(e.getComplemento())
                .build();
    }

    public SolucaoOSOutput converterSolucao(SolucaoOS s) {
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

    public FaturamentoOSOutput converterFaturamento(FaturamentoOS f) {
        if  (f == null) return null;

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
