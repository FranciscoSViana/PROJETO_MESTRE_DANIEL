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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TecnicoService")
class TecnicoServiceTest {

    @Mock
    private TecnicoRepository tecnicoRepository;
    @Mock
    private CredenciadoRepository credenciadoRepository;
    @Mock
    private TecnicoInputDisassembler disassembler;
    @Mock
    private TecnicoOutputAssembler assembler;

    @InjectMocks
    private TecnicoService service;

    private UUID credenciadoId;
    private UUID tecnicoId;
    private Credenciado credenciado;
    private Tecnico tecnico;

    @BeforeEach
    void setUp() {
        credenciadoId = UUID.randomUUID();
        tecnicoId = UUID.randomUUID();

        credenciado = new Credenciado();
        credenciado.setId(credenciadoId);

        tecnico = new Tecnico();
        tecnico.setId(tecnicoId);
        tecnico.setNome("Técnico A");
        tecnico.setCodigo(1L);
        tecnico.setCredenciado(credenciado);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // cpfIsValido() — método estático
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("cpfIsValido() — validação do algoritmo")
    class CpfIsValido {

        @ParameterizedTest(name = "CPF válido: {0}")
        @ValueSource(strings = {
                "529.982.247-25",
                "52998224725",
                "111.444.777-35",
                "11144477735"
        })
        @DisplayName("deve aceitar CPFs matematicamente válidos")
        void deveAceitarCpfsValidos(String cpf) {
            assertThat(TecnicoService.cpfIsValido(cpf)).isTrue();
        }

        @ParameterizedTest(name = "CPF inválido: {0}")
        @ValueSource(strings = {
                "000.000.000-00",
                "111.111.111-11",
                "123.456.789-00",
                "12345678900",
                "999.999.999-99"
        })
        @DisplayName("deve rejeitar CPFs inválidos (sequências repetidas ou dígitos errados)")
        void deveRejeitarCpfsInvalidos(String cpf) {
            assertThat(TecnicoService.cpfIsValido(cpf)).isFalse();
        }

        @Test
        @DisplayName("deve aceitar CPF com máscara (pontos e hífen)")
        void deveAceitarComMascara() {
            assertThat(TecnicoService.cpfIsValido("529.982.247-25")).isTrue();
        }

        @Test
        @DisplayName("deve aceitar CPF sem máscara (apenas dígitos)")
        void deveAceitarSemMascara() {
            assertThat(TecnicoService.cpfIsValido("52998224725")).isTrue();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // salvar()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("salvar()")
    class Salvar {

        @Test
        @DisplayName("deve salvar técnico com sucesso e código sequencial")
        void deveSalvarComSucesso() {
            TecnicoInput input = inputTecnico(null);
            TecnicoOutput output = new TecnicoOutput();

            when(credenciadoRepository.findById(credenciadoId)).thenReturn(Optional.of(credenciado));
            when(tecnicoRepository.findMaxCodigo()).thenReturn(5L);
            when(disassembler.toDomainObject(input)).thenReturn(tecnico);
            when(tecnicoRepository.save(any())).thenReturn(tecnico);
            when(assembler.toModel(tecnico)).thenReturn(output);

            assertThat(service.salvar(credenciadoId, input)).isEqualTo(output);
            assertThat(tecnico.getCodigo()).isEqualTo(6L);
        }

        @Test
        @DisplayName("deve iniciar código em 1 quando não há técnicos anteriores")
        void deveIniciarCodigoEm1() {
            TecnicoInput input = inputTecnico(null);

            when(credenciadoRepository.findById(credenciadoId)).thenReturn(Optional.of(credenciado));
            when(tecnicoRepository.findMaxCodigo()).thenReturn(null);
            when(disassembler.toDomainObject(input)).thenReturn(tecnico);
            when(tecnicoRepository.save(any())).thenReturn(tecnico);
            when(assembler.toModel(tecnico)).thenReturn(new TecnicoOutput());

            service.salvar(credenciadoId, input);

            assertThat(tecnico.getCodigo()).isEqualTo(1L);
        }

        @Test
        @DisplayName("deve lançar CPFInvalidoException quando CPF informado é inválido")
        void deveLancarExcecaoComCpfInvalido() {
            TecnicoInput input = inputTecnico("111.111.111-11");

            when(credenciadoRepository.findById(credenciadoId)).thenReturn(Optional.of(credenciado));
            when(tecnicoRepository.findMaxCodigo()).thenReturn(null);

            assertThatThrownBy(() -> service.salvar(credenciadoId, input))
                    .isInstanceOf(CPFInvalidoException.class)
                    .hasMessageContaining("CPF inválido");
        }

        @Test
        @DisplayName("deve salvar sem validar CPF quando CPF não informado")
        void deveSalvarSemCpf() {
            TecnicoInput input = inputTecnico(null); // sem CPF

            when(credenciadoRepository.findById(credenciadoId)).thenReturn(Optional.of(credenciado));
            when(tecnicoRepository.findMaxCodigo()).thenReturn(0L);
            when(disassembler.toDomainObject(input)).thenReturn(tecnico);
            when(tecnicoRepository.save(any())).thenReturn(tecnico);
            when(assembler.toModel(tecnico)).thenReturn(new TecnicoOutput());

            // Não deve lançar exceção
            service.salvar(credenciadoId, input);
        }

        @Test
        @DisplayName("deve lançar RuntimeException quando credenciado não encontrado")
        void deveLancarExcecaoSeCredenciadoNaoEncontrado() {
            when(credenciadoRepository.findById(credenciadoId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.salvar(credenciadoId, inputTecnico(null)))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Credenciado não encontrado");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // atualizar()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("atualizar()")
    class Atualizar {

        @Test
        @DisplayName("deve atualizar técnico com sucesso")
        void deveAtualizar() {
            TecnicoInput input = inputTecnico(null);
            TecnicoOutput output = new TecnicoOutput();

            when(tecnicoRepository.findById(tecnicoId)).thenReturn(Optional.of(tecnico));
            doNothing().when(disassembler).copyToDomainObject(input, tecnico);
            when(tecnicoRepository.save(tecnico)).thenReturn(tecnico);
            when(assembler.toModel(tecnico)).thenReturn(output);

            assertThat(service.atualizar(tecnicoId, input)).isEqualTo(output);
        }

        @Test
        @DisplayName("deve lançar exceção quando técnico não encontrado")
        void deveLancarExcecaoSeNaoEncontrado() {
            when(tecnicoRepository.findById(tecnicoId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.atualizar(tecnicoId, inputTecnico(null)))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Técnico não encontrado");
        }

        @Test
        @DisplayName("deve lançar CPFInvalidoException ao atualizar com CPF inválido")
        void deveLancarExcecaoComCpfInvalidoNaAtualizacao() {
            TecnicoInput input = inputTecnico("000.000.000-00");
            when(tecnicoRepository.findById(tecnicoId)).thenReturn(Optional.of(tecnico));

            assertThatThrownBy(() -> service.atualizar(tecnicoId, input))
                    .isInstanceOf(CPFInvalidoException.class);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // buscarPorId() e listarPorCredenciado()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("buscarPorId()")
    class BuscarPorId {

        @Test
        @DisplayName("deve retornar output quando técnico encontrado")
        void deveRetornarOutput() {
            TecnicoOutput output = new TecnicoOutput();
            when(tecnicoRepository.findById(tecnicoId)).thenReturn(Optional.of(tecnico));
            when(assembler.toModel(tecnico)).thenReturn(output);

            assertThat(service.buscarPorId(tecnicoId)).isEqualTo(output);
        }

        @Test
        @DisplayName("deve lançar RuntimeException quando técnico não encontrado")
        void deveLancarExcecaoSeNaoEncontrado() {
            when(tecnicoRepository.findById(tecnicoId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.buscarPorId(tecnicoId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Técnico não encontrado");
        }
    }

    @Test
    @DisplayName("listarPorCredenciado() deve retornar página mapeada de técnicos")
    void deveListarPorCredenciado() {
        org.springframework.data.domain.PageRequest pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        org.springframework.data.domain.Page<Tecnico> page =
                new org.springframework.data.domain.PageImpl<>(List.of(tecnico));
        TecnicoOutput output = new TecnicoOutput();

        when(tecnicoRepository.findByCredenciadoId(credenciadoId, pageable)).thenReturn(page);
        when(assembler.toModel(tecnico)).thenReturn(output);

        org.springframework.data.domain.Page<TecnicoOutput> resultado =
                service.listarPorCredenciado(credenciadoId, pageable);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0)).isEqualTo(output);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // excluir() e buscarOuFalhar()
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("excluir() deve chamar deleteById no repositório")
    void deveExcluir() {
        service.excluir(tecnicoId);
        verify(tecnicoRepository).deleteById(tecnicoId);
    }

    @Test
    @DisplayName("buscarOuFalhar() deve lançar TecnicoException quando não encontrado")
    void deveLancarTecnicoException() {
        when(tecnicoRepository.findById(tecnicoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarOuFalhar(tecnicoId))
                .isInstanceOf(TecnicoException.class)
                .hasMessageContaining("Técnico não encontrado");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────
    private TecnicoInput inputTecnico(String cpf) {
        TecnicoInput input = new TecnicoInput();
        input.setNome("Técnico A");
        input.setCpf(cpf);
        return input;
    }
}
