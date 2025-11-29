package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.model.input.ClienteInput;
import io.github.franciscosviana.stmservicos.api.model.output.ClienteOutput;
import io.github.franciscosviana.stmservicos.common.client.model.ReceitaWsResponse;
import io.github.franciscosviana.stmservicos.common.client.BrasilAPIClient;
import io.github.franciscosviana.stmservicos.domain.model.Cliente;
import io.github.franciscosviana.stmservicos.domain.repository.ClienteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final BrasilAPIClient receitaWsClient;
    private final ClienteRepository clienteRepository;

    @Transactional
    public ClienteOutput salvar(ClienteInput clienteInput){

        Long ultimo = clienteRepository.buscarUltimoCodigo();
        Long proximoCodigo = ultimo + 1;

        var dadosCnpj = receitaWsClient.consultarCnpj(clienteInput.getCnpj());

        Cliente cliente = Cliente.builder()
                .id(UUID.randomUUID())
                .codigo(proximoCodigo)
                .contrato(clienteInput.getContrato())
                .nome(clienteInput.getNome())
                .valorChamado(clienteInput.getValorChamado())
                .valorKm(clienteInput.getValorKm())
                .cnpj(clienteInput.getCnpj())
                .inscricaoEstadual(clienteInput.getInscricaoEstadual())
                .razaoSocial(dadosCnpj.getRazaoSocial())
                .build();

        clienteRepository.save(cliente);

        return converter(cliente);
    }

    public ClienteOutput buscarPorId(UUID id) {

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        return converter(cliente);
    }


    public Page<ClienteOutput> listar(Pageable pageable){
        Page<Cliente> clientes = clienteRepository.findAll(pageable);

        return clientes.map(this::converter);
    }

    @Transactional
    public ClienteOutput atualizar(UUID id, ClienteInput clienteInput) {

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        cliente.setContrato(clienteInput.getContrato());
        cliente.setNome(clienteInput.getNome());
        cliente.setValorChamado(clienteInput.getValorChamado());
        cliente.setValorKm(clienteInput.getValorKm());
        cliente.setCnpj(clienteInput.getCnpj());
        cliente.setInscricaoEstadual(clienteInput.getInscricaoEstadual());

        clienteRepository.save(cliente);

        return converter(cliente);
    }

    @Transactional
    public void excluir(UUID id) {

        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        clienteRepository.delete(cliente);
    }



    public ReceitaWsResponse consultarCnpj(String cnpj){
        return receitaWsClient.consultarCnpj(cnpj);
    }

    private ClienteOutput converter(Cliente cliente){
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
}
