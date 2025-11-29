package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.model.input.CredenciadoInput;
import io.github.franciscosviana.stmservicos.api.model.output.CredenciadoOutput;
import io.github.franciscosviana.stmservicos.common.client.BrasilAPIClient;
import io.github.franciscosviana.stmservicos.common.client.model.EstadoResponse;
import io.github.franciscosviana.stmservicos.common.client.model.MunicipioResponse;
import io.github.franciscosviana.stmservicos.domain.model.Credenciado;
import io.github.franciscosviana.stmservicos.domain.repository.CredenciadoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CredenciadoService {

    private final BrasilAPIClient  brasilAPIClient;
    private final CredenciadoRepository credenciadoRepository;

    @Transactional
    public CredenciadoOutput salvar(CredenciadoInput credenciadoInput){

        Long ultimo = credenciadoRepository.buscarUltimoCodigo();
        Long proximoCodigo = ultimo + 1;

        EstadoResponse uf = buscaEstado(credenciadoInput);

        MunicipioResponse cidade = buscaCidade(credenciadoInput);

        Credenciado credenciado = Credenciado.builder()
                .id(UUID.randomUUID())
                .codigo(proximoCodigo)
                .rag(credenciadoInput.getRag())
                .cidade(cidade.getNome())
                .uf(uf.getSigla())
                .tipo(credenciadoInput.getTipo())
                .valorChamado(credenciadoInput.getValorChamado())
                .quantidadeOSAtendidas(credenciadoInput.getQuantidadeOSAtendidas())
                .contato(credenciadoInput.getContato())
                .telefones(credenciadoInput.getTelefones())
                .email(credenciadoInput.getEmail())
                .tecnico(credenciadoInput.getTecnico())
                .cpf(credenciadoInput.getCpf())
                .base(credenciadoInput.getBase())
                .build();

        credenciadoRepository.save(credenciado);

        return converter(credenciado);
    }

    private MunicipioResponse buscaCidade(CredenciadoInput credenciadoInput) {
        List<MunicipioResponse> municipios = brasilAPIClient.buscarMunicipios(credenciadoInput.getUf());

        return municipios.stream()
                .filter(m -> m.getNome().equalsIgnoreCase(credenciadoInput.getCidade()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Cidade inválida para a UF: " + credenciadoInput.getUf() + " : " + credenciadoInput.getCidade()
                ));
    }

    private EstadoResponse buscaEstado(CredenciadoInput credenciadoInput) {
        List<EstadoResponse> estados = brasilAPIClient.buscarEstados();

        return estados.stream()
                .filter(e -> e.getSigla().equalsIgnoreCase(credenciadoInput.getUf()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "UF Inválida: " + credenciadoInput.getUf()
                ));
    }

    public CredenciadoOutput buscarPorId(UUID id) {

        Credenciado credenciado = credenciadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Credenciado não encontrado"));

        return converter(credenciado);
    }

    public Page<CredenciadoOutput> listar(Pageable pageable){
        Page<Credenciado> credenciados = credenciadoRepository.findAll(pageable);

        return credenciados.map(this::converter);
    }

    @Transactional
    public CredenciadoOutput atualizar(UUID id, CredenciadoInput credenciadoInput) {

        Credenciado credenciado = credenciadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));

        credenciado.setRag(credenciadoInput.getRag());
        credenciado.setCidade(credenciadoInput.getCidade());
        credenciado.setUf(credenciadoInput.getUf());
        credenciado.setTipo(credenciadoInput.getTipo());
        credenciado.setValorChamado(credenciadoInput.getValorChamado());
        credenciado.setValorKm(credenciadoInput.getValorKm());
        credenciado.setQuantidadeOSAtendidas(credenciadoInput.getQuantidadeOSAtendidas());
        credenciado.setContato(credenciadoInput.getContato());
        credenciado.setTelefones(credenciadoInput.getTelefones());
        credenciado.setEmail(credenciadoInput.getEmail());
        credenciado.setTecnico(credenciadoInput.getTecnico());
        credenciado.setCpf(credenciadoInput.getCpf());
        credenciado.setBase(credenciadoInput.getBase());

        credenciadoRepository.save(credenciado);

        return converter(credenciado);
    }

    @Transactional
    public void excluir(UUID id) {

        Credenciado credenciado = credenciadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Credenciado não encontrado"));

        credenciadoRepository.delete(credenciado);
    }

    public List<EstadoResponse> listarEstados() {
        List<EstadoResponse> estados = brasilAPIClient.buscarEstados();

        return estados.stream()
                .sorted(Comparator.comparing(EstadoResponse::getNome))
                .toList();
    }

    public List<MunicipioResponse> listarMunicipios(String uf) {
        brasilAPIClient.buscarEstados().stream()
                .filter(e -> e.getSigla().equalsIgnoreCase(uf))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("UF inválida: " + uf));

        return brasilAPIClient.buscarMunicipios(uf);
    }

    private CredenciadoOutput converter(Credenciado  credenciado){
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
}
