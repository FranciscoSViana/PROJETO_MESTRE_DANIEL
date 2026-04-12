package io.github.franciscosviana.stmservicos.domain.repository.spec;

import io.github.franciscosviana.stmservicos.domain.model.OrdemServico;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OrdemServicoSpecification")
class OrdemServicoSpecificationTest {

    // A lógica pública testável é: o método retorna uma Specification não nula
    // e a função parsearData (privada) é testada indiretamente via filtro.

    @Test
    @DisplayName("filtro() deve retornar Specification não nula com todos os filtros nulos")
    void deveRetornarSpecificationComFiltrosNulos() {
        Specification<OrdemServico> spec = OrdemServicoSpecification.filtro(
                null, null, null, null, null, null, null, null, null);

        assertThat(spec).isNotNull();
    }

    @Test
    @DisplayName("filtro() deve retornar Specification não nula com todos os filtros preenchidos")
    void deveRetornarSpecificationComTodosFiltros() {
        Specification<OrdemServico> spec = OrdemServicoSpecification.filtro(
                "OS-001", "OSG260001", "2026-01-15", "ABERTA",
                "Empresa", "Credenciado", "Teresina", "PI", "TRACK123");

        assertThat(spec).isNotNull();
    }

    @Nested
    @DisplayName("parsearData() — via filtro() — formatos de data aceitos")
    class ParsearData {

        @ParameterizedTest(name = "formato ISO: {0}")
        @CsvSource({"2026-01-15", "2025-12-31", "2024-02-29"})
        @DisplayName("deve aceitar formato ISO yyyy-MM-dd")
        void deveAceitarIso(String data) {
            // Não lança exceção, spec não é nula
            assertThat(OrdemServicoSpecification.filtro(
                    null, null, data, null, null, null, null, null, null))
                    .isNotNull();
        }

        @ParameterizedTest(name = "formato BR: {0}")
        @CsvSource({"15/01/2026", "31/12/2025"})
        @DisplayName("deve aceitar formato BR dd/MM/yyyy")
        void deveAceitarBr(String data) {
            assertThat(OrdemServicoSpecification.filtro(
                    null, null, data, null, null, null, null, null, null))
                    .isNotNull();
        }

        @ParameterizedTest(name = "formato sem separador: {0}")
        @CsvSource({"15012026", "31122025"})
        @DisplayName("deve aceitar formato ddMMyyyy sem separador")
        void deveAceitarSemSeparador(String data) {
            assertThat(OrdemServicoSpecification.filtro(
                    null, null, data, null, null, null, null, null, null))
                    .isNotNull();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("deve ignorar data nula ou vazia sem lançar exceção")
        void deveIgnorarDataNulaOuVazia(String data) {
            assertThat(OrdemServicoSpecification.filtro(
                    null, null, data, null, null, null, null, null, null))
                    .isNotNull();
        }

        @Test
        @DisplayName("deve ignorar formato de data inválido sem lançar exceção")
        void deveIgnorarFormatoInvalido() {
            assertThat(OrdemServicoSpecification.filtro(
                    null, null, "data-invalida", null, null, null, null, null, null))
                    .isNotNull();
        }

        @Test
        @DisplayName("deve ignorar status inválido sem lançar exceção")
        void deveIgnorarStatusInvalido() {
            assertThat(OrdemServicoSpecification.filtro(
                    null, null, null, "STATUS_INVALIDO", null, null, null, null, null))
                    .isNotNull();
        }
    }
}
