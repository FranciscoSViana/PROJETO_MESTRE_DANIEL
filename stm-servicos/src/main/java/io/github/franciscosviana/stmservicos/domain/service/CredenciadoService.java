package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.assembler.CredenciadoInputDisassembler;
import io.github.franciscosviana.stmservicos.api.assembler.CredenciadoOutputAssembler;
import io.github.franciscosviana.stmservicos.api.model.input.CredenciadoInput;
import io.github.franciscosviana.stmservicos.api.model.output.CredenciadoOutput;
import io.github.franciscosviana.stmservicos.common.client.BrasilAPIClient;
import io.github.franciscosviana.stmservicos.common.client.model.EstadoResponse;
import io.github.franciscosviana.stmservicos.common.client.model.MunicipioResponse;
import io.github.franciscosviana.stmservicos.common.validation.CredenciadoException;
import io.github.franciscosviana.stmservicos.domain.model.Credenciado;
import io.github.franciscosviana.stmservicos.domain.model.enums.TipoPessoa;
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

    private final BrasilAPIClient brasilAPIClient;
    private final CredenciadoOutputAssembler assembler;
    private final CredenciadoInputDisassembler disassembler;
    private final CredenciadoRepository credenciadoRepository;

    @Transactional
    public CredenciadoOutput salvar(CredenciadoInput credenciadoInput) {

        validarNumeroPessoa(credenciadoInput.getTipoPessoa(), credenciadoInput.getNumeroPessoa());

        Long ultimo = credenciadoRepository.buscarUltimoCodigo();
        Long proximoCodigo = ultimo + 1;

        Credenciado credenciado = disassembler.toDomainObject(credenciadoInput);

        credenciado.setId(UUID.randomUUID());
        credenciado.setCodigo(proximoCodigo);

        credenciadoRepository.save(credenciado);

        return assembler.toModel(credenciado);
    }


    public CredenciadoOutput buscarPorId(UUID id) {

        Credenciado credenciado = buscarOuFalhar(id);

        return assembler.toModel(credenciado);
    }

    public Page<CredenciadoOutput> listar(Pageable pageable) {

        return credenciadoRepository.findAll(pageable)
                .map(assembler::toModel);
    }

    @Transactional
    public CredenciadoOutput atualizar(UUID id, CredenciadoInput credenciadoInput) {

        Credenciado credenciado = buscarOuFalhar(id);

        disassembler.copyToDomainObject(credenciadoInput, credenciado);

        credenciadoRepository.save(credenciado);

        return assembler.toModel(credenciado);
    }

    @Transactional
    public void excluir(UUID id) {

        Credenciado credenciado = buscarOuFalhar(id);

        credenciadoRepository.delete(credenciado);
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

        if (numeroPessoa == null || numeroPessoa.isBlank()) {
            throw new CredenciadoException("Número do documento é obrigatório");
        }

        TipoPessoa tipoPessoa = TipoPessoa.fromCodigo(tipoPessoaCodigo);

        boolean invalido = switch (tipoPessoa) {
            case PESSOA_FISICA -> !isCPFValido(numeroPessoa);
            case PESSOA_JURIDICA -> !isCNPJValido(numeroPessoa);
        };

        if (invalido) {
            throw new CredenciadoException(tipoPessoa.getDescricao() + " inválido: " + numeroPessoa);
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
