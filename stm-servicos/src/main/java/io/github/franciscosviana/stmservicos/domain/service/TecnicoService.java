package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.assembler.TecnicoInputDisassembler;
import io.github.franciscosviana.stmservicos.api.assembler.TecnicoOutputAssembler;
import io.github.franciscosviana.stmservicos.api.model.input.TecnicoInput;
import io.github.franciscosviana.stmservicos.api.model.output.TecnicoOutput;
import io.github.franciscosviana.stmservicos.common.validation.CPFInvalidoException;
import io.github.franciscosviana.stmservicos.common.validation.TecnicoException;
import io.github.franciscosviana.stmservicos.domain.model.Credenciado;
import io.github.franciscosviana.stmservicos.domain.model.Tecnico;
import io.github.franciscosviana.stmservicos.domain.repository.CredenciadoRepository;
import io.github.franciscosviana.stmservicos.domain.repository.TecnicoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TecnicoService {

    private final TecnicoRepository tecnicoRepository;
    private final CredenciadoRepository credenciadoRepository;
    private final TecnicoInputDisassembler disassembler;
    private final TecnicoOutputAssembler assembler;

    @Transactional
    public TecnicoOutput salvar(UUID credenciadoId, TecnicoInput input) {

        Credenciado credenciado = credenciadoRepository.findById(credenciadoId)
                .orElseThrow(() -> new RuntimeException("Credenciado não encontrado"));

        Long ultimoCodigo = tecnicoRepository.findMaxCodigo();
        Long proximoCodigo = ultimoCodigo == null ? 1L : ultimoCodigo + 1;

        validacaoCPF(input);


        Tecnico tecnico = disassembler.toDomainObject(input);
        tecnico.setId(UUID.randomUUID());
        tecnico.setCodigo(proximoCodigo);
        tecnico.setCredenciado(credenciado);

        tecnicoRepository.save(tecnico);

        return assembler.toModel(tecnico);
    }

    private void validacaoCPF(TecnicoInput input) {
        if (input.getCpf() != null && !input.getCpf().isBlank()) {

            String cpfRecebido = input.getCpf();
            boolean cpfValido = cpfIsValido(cpfRecebido);

            System.out.println("========== DEBUG CPF ==========");
            System.out.println("CPF recebido: [" + cpfRecebido + "]");
            System.out.println("CPF normalizado: [" + cpfRecebido.replaceAll("\\D", "") + "]");
            System.out.println("Resultado cpfIsValido: " + cpfValido);
            System.out.println("================================");

            if (!cpfValido) {
                throw new CPFInvalidoException("CPF inválido");
            }
        }
    }

    public TecnicoOutput buscarPorId(UUID tecnicoId) {
        Tecnico tecnico = tecnicoRepository.findById(tecnicoId)
                .orElseThrow(() -> new RuntimeException("Técnico não encontrado"));

        return assembler.toModel(tecnico);
    }

    public Page<TecnicoOutput> listarPorCredenciado(UUID credenciadoId, Pageable pageable) {
        return tecnicoRepository.findByCredenciadoId(credenciadoId, pageable)
                .map(assembler::toModel);
    }

    @Transactional
    public TecnicoOutput atualizar(UUID tecnicoId, TecnicoInput input) {

        Tecnico tecnico = tecnicoRepository.findById(tecnicoId)
                .orElseThrow(() -> new RuntimeException("Técnico não encontrado"));

        validacaoCPF(input);

        disassembler.copyToDomainObject(input, tecnico);

        tecnicoRepository.save(tecnico);

        return assembler.toModel(tecnico);
    }

    @Transactional
    public void excluir(UUID tecnicoId) {
        tecnicoRepository.deleteById(tecnicoId);
    }

    public Tecnico buscarOuFalhar(UUID tecnicoId) {
        return tecnicoRepository.findById(tecnicoId)
                .orElseThrow(() -> new TecnicoException("Técnico não encontrado"));
    }

    public static boolean cpfIsValido(String cpf) {
        cpf = cpf.replaceAll("\\D", "");

        if (cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        try {
            int soma = 0;
            for (int i = 0; i < 9; i++) {
                soma += (cpf.charAt(i) - '0') * (10 - i);
            }
            int digito1 = 11 - (soma % 11);
            digito1 = (digito1 >= 10) ? 0 : digito1;

            soma = 0;
            for (int i = 0; i < 10; i++) {
                soma += (cpf.charAt(i) - '0') * (11 - i);
            }
            int digito2 = 11 - (soma % 11);
            digito2 = (digito2 >= 10) ? 0 : digito2;

            return digito1 == (cpf.charAt(9) - '0')
                    && digito2 == (cpf.charAt(10) - '0');
        } catch (Exception e) {
            return false;
        }
    }
}
