package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.assembler.OrdemServicoInputDisassembler;
import io.github.franciscosviana.stmservicos.api.assembler.OrdemServicoOutputAssembler;
import io.github.franciscosviana.stmservicos.api.model.input.OrdemServicoInput;
import io.github.franciscosviana.stmservicos.api.model.output.OrdemServicoOutput;
import io.github.franciscosviana.stmservicos.common.validation.ContratoException;
import io.github.franciscosviana.stmservicos.common.validation.OrdemServicoException;
import io.github.franciscosviana.stmservicos.domain.model.Cliente;
import io.github.franciscosviana.stmservicos.domain.model.Contrato;
import io.github.franciscosviana.stmservicos.domain.model.Credenciado;
import io.github.franciscosviana.stmservicos.domain.model.OrdemServico;
import io.github.franciscosviana.stmservicos.domain.model.Tecnico;
import io.github.franciscosviana.stmservicos.domain.model.enums.StatusOrdem;
import io.github.franciscosviana.stmservicos.domain.model.enums.TipoAcaoOS;
import io.github.franciscosviana.stmservicos.domain.repository.OrdemServicoRepository;
import io.github.franciscosviana.stmservicos.domain.repository.spec.OrdemServicoSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrdemServicoService {

    private final TecnicoService tecnicoService;
    private final ClienteService clienteService;
    private final ContratoService contratoService;
    private final CredenciadoService credenciadoService;
    private final OrdemServicoRepository repository;
    private final OrdemServicoOutputAssembler assembler;
    private final OrdemServicoInputDisassembler disassembler;
    private final HistoricoOrdemServicoService historicoOrdemServicoService;

    @Transactional
    public OrdemServicoOutput salvar(OrdemServicoInput input) {

        OrdemServico ordem = disassembler.toDomainObject(input);

        ordem.setOsg(gerarProximoOsg());

        // Cliente
        var cliente = clienteService.buscarOuFalhar(input.getClienteId());

        // Contrato
        if (input.getContratoId() == null) {
            throw new ContratoException("Contrato não informado.");
        }
        var contrato = contratoService.buscarOuFalhar(input.getContratoId());

        if (!contrato.getCliente().getId().equals(cliente.getId())) {
            throw new ContratoException("Contrato não pertence ao cliente");
        }

        // Técnico (OBRIGATÓRIO)
        if (input.getTecnicoId() == null) {
            throw new OrdemServicoException("Técnico é obrigatório para criação da OS");
        }

        Tecnico tecnico = tecnicoService.buscarOuFalhar(input.getTecnicoId());

        // Integridade: técnico pertence ao credenciado?
        if (!tecnico.getCredenciado().getId().equals(input.getCredenciadoId())) {
            throw new OrdemServicoException("Técnico não pertence ao credenciado informado");
        }

        // Set entidades gerenciadas
        ordem.setCliente(cliente);
        ordem.setContrato(contrato);
        ordem.setTecnico(tecnico);

        repository.save(ordem);

        historicoOrdemServicoService.registrar(ordem, TipoAcaoOS.CRIACAO, "Ordem de Serviço criada com sucesso");

        return assembler.toModel(ordem);
    }


    public OrdemServicoOutput buscarPorId(UUID id) {

        OrdemServico ordemServico = repository.buscarCompleta(id)
                .orElseThrow(() -> new OrdemServicoException("Ordem de Serviço não encontrada"));

        return assembler.toModel(ordemServico);
    }

    public Page<OrdemServicoOutput> listar(
            Pageable pageable,
            String osClt,
            String osg,
            String dataAbertura,
            String status,
            String cliente,
            String credenciado,
            String cidade,
            String estado,
            String rastreio
    ) {

        Specification<OrdemServico> spec =
                OrdemServicoSpecification.filtro(
                        osClt, osg, dataAbertura, status, cliente,
                        credenciado, cidade, estado, rastreio
                );

        return repository.findAll(spec, pageable)
                .map(assembler::toModel);
    }

    @Transactional
    public OrdemServicoOutput atualizar(UUID id, OrdemServicoInput input) {

        OrdemServico atual = buscarOuFalhar(id);

        // 🔥 CAPTURA O ESTADO ANTERIOR PARA COMPARAÇÃO
        OrdemServico estadoAnterior = clonarParaComparacao(atual);

        disassembler.copyToDomainObject(input, atual);

        // Cliente
        Cliente cliente = clienteService.buscarOuFalhar(atual.getCliente().getId());

        // Contrato
        if (atual.getContrato() == null || atual.getContrato().getId() == null) {
            throw new ContratoException("Contrato não informado");
        }

        Contrato contrato = contratoService.buscarOuFalhar(atual.getContrato().getId());

        if (!contrato.getCliente().getId().equals(cliente.getId())) {
            throw new ContratoException("Contrato não pertence ao cliente");
        }

        atual.setCliente(cliente);
        atual.setContrato(contrato);

        // 🔥 TRATAMENTO DO CREDENCIADO
        if (input.getCredenciadoId() != null) {
            Credenciado credenciado = credenciadoService.buscarOuFalhar(input.getCredenciadoId());
            atual.setCredenciado(credenciado);
        }

        // TRATAMENTO DO TÉCNICO
        if (input.getTecnicoId() != null) {

            Tecnico tecnico = tecnicoService.buscarOuFalhar(input.getTecnicoId());

            if (!tecnico.getCredenciado().getId().equals(atual.getCredenciado().getId())) {
                throw new OrdemServicoException("Técnico não pertence ao credenciado informado");
            }

            atual.setTecnico(tecnico);
        }

        repository.save(atual);

        // 🔥 GERA DESCRIÇÃO DETALHADA DAS ALTERAÇÕES
        String descricaoAlteracoes = gerarDescricaoAlteracoes(estadoAnterior, atual);

        if (estadoAnterior.getStatus() != atual.getStatus()) {
            historicoOrdemServicoService.registrar(
                    atual,
                    TipoAcaoOS.MUDANCA_STATUS,
                    descricaoAlteracoes
            );
        } else {
            historicoOrdemServicoService.registrar(
                    atual,
                    TipoAcaoOS.ATUALIZACAO,
                    descricaoAlteracoes
            );
        }

        return assembler.toModel(atual);
    }

    @Transactional
    public void deletar(UUID id) {
        OrdemServico ordemServico = buscarOuFalhar(id);

        // 1️⃣ Deleta todos os históricos primeiro
        historicoOrdemServicoService.deletarPorOrdemServico(ordemServico.getId());

        // 2️⃣ Remove a ordem de serviço
        repository.delete(ordemServico);
    }

    public String gerarProximoOsg() {

        String anoAtual = String.format("%02d", OffsetDateTime.now().getYear() % 100);

        List<String> result = repository.findLastOsg(PageRequest.of(0, 1));

        if (result.isEmpty()) {
            return "OSG" + anoAtual + "0001";
        }

        String ultimoOsg = result.get(0);

        String anoUltimo = ultimoOsg.substring(3, 5);
        int sequencia = Integer.parseInt(ultimoOsg.substring(5));

        if (!anoUltimo.equals(anoAtual)) {
            return "OSG" + anoAtual + "0001";
        }

        sequencia++;

        return "OSG" + anoAtual + String.format("%04d", sequencia);
    }

    private OrdemServico buscarOuFalhar(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new OrdemServicoException("Ordem de Serviço não encontrada"));
    }

    // 🔥 MÉTODO AUXILIAR: Clona valores relevantes para comparação
    private OrdemServico clonarParaComparacao(OrdemServico os) {
        OrdemServico clone = new OrdemServico();
        clone.setOsClt(os.getOsClt());
        clone.setStatus(os.getStatus());
        clone.setContato(os.getContato());
        clone.setDepartamento(os.getDepartamento());
        clone.setTelefone(os.getTelefone());
        clone.setAcionador(os.getAcionador());
        clone.setEquipamento(os.getEquipamento());
        clone.setSerie(os.getSerie());
        clone.setPib(os.getPib());
        clone.setDefeito(os.getDefeito());
        clone.setRastreio(os.getRastreio());
        clone.setCliente(os.getCliente());
        clone.setContrato(os.getContrato());
        clone.setTecnico(os.getTecnico());
        clone.setCredenciado(os.getCredenciado()); // ✅ Já estava aqui
        return clone;
    }

    // 🔥 MÉTODO PRINCIPAL: Gera descrição detalhada das alterações
    private String gerarDescricaoAlteracoes(OrdemServico anterior, OrdemServico atual) {
        List<String> alteracoes = new ArrayList<>();

        // Status
        if (!Objects.equals(anterior.getStatus(), atual.getStatus())) {
            alteracoes.add(String.format("Status: %s → %s",
                    anterior.getStatus(), atual.getStatus()));
        }

        // OS Cliente
        if (!Objects.equals(anterior.getOsClt(), atual.getOsClt())) {
            alteracoes.add(String.format("OS Cliente: %s → %s",
                    valorOuVazio(anterior.getOsClt()), valorOuVazio(atual.getOsClt())));
        }

        // Contato
        if (!Objects.equals(anterior.getContato(), atual.getContato())) {
            alteracoes.add(String.format("Contato: %s → %s",
                    valorOuVazio(anterior.getContato()), valorOuVazio(atual.getContato())));
        }

        // Departamento
        if (!Objects.equals(anterior.getDepartamento(), atual.getDepartamento())) {
            alteracoes.add(String.format("Departamento: %s → %s",
                    valorOuVazio(anterior.getDepartamento()), valorOuVazio(atual.getDepartamento())));
        }

        // Telefone
        if (!Objects.equals(anterior.getTelefone(), atual.getTelefone())) {
            alteracoes.add(String.format("Telefone: %s → %s",
                    valorOuVazio(anterior.getTelefone()), valorOuVazio(atual.getTelefone())));
        }

        // Acionador
        if (!Objects.equals(anterior.getAcionador(), atual.getAcionador())) {
            alteracoes.add(String.format("Acionador: %s → %s",
                    valorOuVazio(anterior.getAcionador()), valorOuVazio(atual.getAcionador())));
        }

        // Equipamento
        if (!Objects.equals(anterior.getEquipamento(), atual.getEquipamento())) {
            alteracoes.add(String.format("Equipamento: %s → %s",
                    valorOuVazio(anterior.getEquipamento()), valorOuVazio(atual.getEquipamento())));
        }

        // Série
        if (!Objects.equals(anterior.getSerie(), atual.getSerie())) {
            alteracoes.add(String.format("Série: %s → %s",
                    valorOuVazio(anterior.getSerie()), valorOuVazio(atual.getSerie())));
        }

        // PIB
        if (!Objects.equals(anterior.getPib(), atual.getPib())) {
            alteracoes.add(String.format("PIB: %s → %s",
                    valorOuVazio(anterior.getPib()), valorOuVazio(atual.getPib())));
        }

        // Defeito
        if (!Objects.equals(anterior.getDefeito(), atual.getDefeito())) {
            alteracoes.add(String.format("Defeito: %s → %s",
                    valorOuVazio(anterior.getDefeito()), valorOuVazio(atual.getDefeito())));
        }

        // Rastreio
        if (!Objects.equals(anterior.getRastreio(), atual.getRastreio())) {
            alteracoes.add(String.format("Rastreio: %s → %s",
                    valorOuVazio(anterior.getRastreio()), valorOuVazio(atual.getRastreio())));
        }

        // Cliente
        if (anterior.getCliente() != null && atual.getCliente() != null
                && !anterior.getCliente().getId().equals(atual.getCliente().getId())) {
            alteracoes.add("Cliente alterado");
        }

        // Contrato
        if (anterior.getContrato() != null && atual.getContrato() != null
                && !anterior.getContrato().getId().equals(atual.getContrato().getId())) {
            alteracoes.add("Contrato alterado");
        }

        // Técnico
        if (anterior.getTecnico() != null && atual.getTecnico() != null
                && !anterior.getTecnico().getId().equals(atual.getTecnico().getId())) {
            alteracoes.add(String.format("Técnico alterado: %s → %s",
                    anterior.getTecnico().getNome(), atual.getTecnico().getNome()));
        }

        // Credenciado
        if (anterior.getCredenciado() != null && atual.getCredenciado() != null
                && !anterior.getCredenciado().getId().equals(atual.getCredenciado().getId())) {
            alteracoes.add(String.format("Credenciado alterado: %s → %s",
                    anterior.getCredenciado().getRag(), atual.getCredenciado().getRag()));
        }

        // Retorna descrição concatenada ou mensagem padrão
        if (alteracoes.isEmpty()) {
            return "Dados da OS atualizados";
        }

        return String.join(" | ", alteracoes);
    }

    // 🔥 MÉTODO AUXILIAR: Retorna o valor ou "vazio"
    private String valorOuVazio(Object valor) {
        return valor != null && !valor.toString().isBlank() ? valor.toString() : "(vazio)";
    }
}