package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.assembler.CredenciadoInputDisassembler;
import io.github.franciscosviana.stmservicos.api.assembler.CredenciadoOutputAssembler;
import io.github.franciscosviana.stmservicos.api.model.input.CredenciadoInput;
import io.github.franciscosviana.stmservicos.api.model.output.CredenciadoOutput;
import io.github.franciscosviana.stmservicos.common.client.BrasilAPIClient;
import io.github.franciscosviana.stmservicos.common.client.GoogleMapsClient;
import io.github.franciscosviana.stmservicos.common.client.model.EstadoResponse;
import io.github.franciscosviana.stmservicos.common.client.model.MunicipioResponse;
import io.github.franciscosviana.stmservicos.common.validation.CredenciadoException;
import io.github.franciscosviana.stmservicos.domain.model.Credenciado;
import io.github.franciscosviana.stmservicos.domain.model.Endereco;
import io.github.franciscosviana.stmservicos.domain.model.GeoLocation;
import io.github.franciscosviana.stmservicos.domain.model.enums.TipoPessoa;
import io.github.franciscosviana.stmservicos.domain.repository.CredenciadoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CredenciadoService {

    private final BrasilAPIClient brasilAPIClient;
    private final GoogleMapsClient googleMapsClient;
    private final CredenciadoOutputAssembler assembler;
    private final CredenciadoInputDisassembler disassembler;
    private final CredenciadoRepository credenciadoRepository;

    /* ============================
      SALVAR
      ============================ */
    @Transactional
    public CredenciadoOutput salvar(CredenciadoInput input) {

        log.info("📥 Iniciando salvamento de credenciado");
        log.info("➡️ Input recebido: {}", input);

        validarNumeroPessoa(input.getTipoPessoa(), input.getNumeroPessoa());

        Long ultimo = credenciadoRepository.buscarUltimoCodigo();
        Long proximoCodigo = ultimo + 1;

        Credenciado credenciado = disassembler.toDomainObject(input);
        credenciado.setId(UUID.randomUUID());
        credenciado.setCodigo(proximoCodigo);

        log.info("📦 Endereço após disassembler: {}", credenciado.getEndereco());

        resolverGeoCredenciado(credenciado);

        log.info("✅ Credenciado salvo com sucesso. {}", credenciado);

        credenciadoRepository.save(credenciado);

        log.info("✅ Credenciado salvo com sucesso. Código: {}", credenciado.getCodigo());

        return assembler.toModel(credenciado);
    }

    /* ============================
       ATUALIZAR
       ============================ */
    @Transactional
    public CredenciadoOutput atualizar(UUID id, CredenciadoInput input) {

        Credenciado credenciado = buscarOuFalhar(id);
        disassembler.copyToDomainObject(input, credenciado);

        resolverGeoCredenciado(credenciado);

        credenciadoRepository.save(credenciado);
        return assembler.toModel(credenciado);
    }


    @Transactional
    public void excluir(UUID id) {

        Credenciado credenciado = buscarOuFalhar(id);

        credenciadoRepository.delete(credenciado);
    }

    @Transactional
    public void atualizarGeoTodos() {
        List<Credenciado> credenciados = credenciadoRepository.findAll();

        for (Credenciado c : credenciados) {
            if (c.getGeoLocation() == null) {
                Endereco e = c.getEndereco();

                String enderecoCompleto = String.format(
                        "%s, %s, %s, %s, Brasil",
                        e.getLogradouro(),
                        e.getNumero(),
                        e.getCidade(),
                        e.getEstado()
                );

                try {
                    GeoLocation geo = googleMapsClient.buscarPorEndereco(enderecoCompleto);
                    c.setGeoLocation(geo);
                } catch (Exception ex) {
                    log.warn("Não foi possível geocodificar credenciado {}", c.getCodigo());
                }
            }
        }
    }

    /* ============================
       GEOLOCALIZAÇÃO
       ============================ */
    private void resolverGeoCredenciado(Credenciado credenciado) {

        Endereco e = credenciado.getEndereco();

        log.info("📍 Resolvendo geolocalização do credenciado {}", credenciado.getCodigo());

        // Endereço completo detalhado
        String enderecoCompleto = String.format(
                "%s, %s, %s, %s, %s, Brasil",
                e.getLogradouro(),
                e.getNumero(),
                e.getBairro(),
                e.getCidade(),
                e.getEstado()
        );

        GeoLocation geo = null;

        try {
            geo = googleMapsClient.buscarPorEndereco(enderecoCompleto);
            log.info("📌 Geolocalização obtida pelo endereço completo: lat={}, lng={}",
                    geo.getLatitude(), geo.getLongitude());
        } catch (Exception ex) {
            log.warn("❌ Endereço completo não encontrado, tentando pelo CEP: {}", e.getCep());

            // Fallback: buscar apenas pelo CEP
            try {
                geo = googleMapsClient.buscarPorEndereco(e.getCep() + ", Brasil");
                log.info("📌 Geolocalização obtida pelo CEP: lat={}, lng={}",
                        geo.getLatitude(), geo.getLongitude());
            } catch (Exception ex2) {
                log.error("🚨 Não foi possível geocodificar endereço nem CEP: {}", e.getCep());
                throw new CredenciadoException(
                        "CEP não possui geolocalização: Endereço sem geolocalização"
                );
            }
        }

        credenciado.setGeoLocation(geo);
    }


    /* ============================
       BUSCAR PRÓXIMOS
       ============================ */
    public List<CredenciadoOutput> buscarProximosPorCep(String cep, Double raioKm) {
        GeoLocation origem = null;

        try {
            // Tenta buscar a geolocalização do CEP
            origem = googleMapsClient.buscarPorEndereco(cep + ", Brasil");
        } catch (Exception e) {
            // Caso haja falha, loga o erro e não realiza a busca
            log.warn("Falha ao buscar geolocalização para o CEP: {}. Erro: {}", cep, e.getMessage());
            throw new RuntimeException("Não foi possível buscar a geolocalização para o CEP fornecido.");
        }

        // Se a geolocalização foi encontrada, realiza a busca dos credenciados próximos
        if (origem != null) {
            log.info("Geolocalização encontrada para o CEP: {}. Latitude: {}, Longitude: {}", cep, origem.getLatitude(), origem.getLongitude());

            // Busca no repositório os credenciados dentro do raio de 50 km
            List<CredenciadoOutput> credenciadosProximos = credenciadoRepository.buscarPorRaio(
                            origem.getLatitude(),
                            origem.getLongitude(),
                            raioKm
                    ).stream()
                    .map(assembler::toModel)
                    .collect(Collectors.toList());

            if (credenciadosProximos.isEmpty()) {
                log.info("Nenhum credenciado encontrado dentro do raio de {} km para o CEP: {}", raioKm, cep);
            }

            return credenciadosProximos;
        }

        // Caso não tenha sido possível encontrar a geolocalização
        log.error("Não foi possível encontrar credenciados próximos para o CEP: {}", cep);
        return List.of();
    }

    public CredenciadoOutput buscarPorId(UUID id) {

        Credenciado credenciado = buscarOuFalhar(id);

        return assembler.toModel(credenciado);
    }

    public Page<CredenciadoOutput> listar(Pageable pageable) {

        return credenciadoRepository.findAll(pageable)
                .map(assembler::toModel);
    }

    public CredenciadoOutput buscarPorCodigo(Long codigo) {
        Credenciado credenciado = credenciadoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado pelo código: " + codigo));

        return assembler.toModel(credenciado);
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

    private Credenciado buscarOuFalhar(UUID id) {
        return credenciadoRepository.findById(id)
                .orElseThrow(() -> new CredenciadoException("Credenciado não encontrado"));
    }

    private void validarNumeroPessoa(Integer tipoPessoaCodigo, String numeroPessoa) {

        // 🔹 Se não foi informado, não valida (regra do front)
        if (numeroPessoa == null || numeroPessoa.isBlank()) {
            return;
        }

        TipoPessoa tipoPessoa = TipoPessoa.fromCodigo(tipoPessoaCodigo);

        boolean invalido = switch (tipoPessoa) {
            case PESSOA_FISICA -> !isCPFValido(numeroPessoa);
            case PESSOA_JURIDICA -> !isCNPJValido(numeroPessoa);
        };

        if (invalido) {
            throw new CredenciadoException(
                    tipoPessoa.getDescricao() + " inválido: " + numeroPessoa
            );
        }
    }

    private boolean isCPFValido(String cpf) {
        if (cpf == null || cpf.isBlank()) return false;

        cpf = cpf.replaceAll("\\D", "");

        if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) return false;

        try {
            int sum1 = 0;

            for (int i = 0; i < 9; i++) sum1 += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
            int check1 = 11 - (sum1 % 11);

            if (check1 >= 10) check1 = 0;
            if (check1 != Character.getNumericValue(cpf.charAt(9))) return false;

            int sum2 = 0;
            for (int i = 0; i < 10; i++) sum2 += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
            int check2 = 11 - (sum2 % 11);
            if (check2 >= 10) check2 = 0;
            return check2 == Character.getNumericValue(cpf.charAt(10));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isCNPJValido(String cnpj) {
        if (cnpj == null) return false;

        cnpj = cnpj.replaceAll("\\D", "");

        if (cnpj.length() != 14 || cnpj.matches("(\\d)\\1{13}")) return false;

        try {
            int[] peso1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            int[] peso2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

            int soma = 0;
            for (int i = 0; i < 12; i++) {
                soma += Character.getNumericValue(cnpj.charAt(i)) * peso1[i];
            }

            int dig1 = soma % 11;
            dig1 = dig1 < 2 ? 0 : 11 - dig1;

            if (dig1 != Character.getNumericValue(cnpj.charAt(12))) return false;

            soma = 0;
            for (int i = 0; i < 13; i++) {
                soma += Character.getNumericValue(cnpj.charAt(i)) * peso2[i];
            }

            int dig2 = soma % 11;
            dig2 = dig2 < 2 ? 0 : 11 - dig2;

            return dig2 == Character.getNumericValue(cnpj.charAt(13));

        } catch (Exception e) {
            return false;
        }
    }
}
