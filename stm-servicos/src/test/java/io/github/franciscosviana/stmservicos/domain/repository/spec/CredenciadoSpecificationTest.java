package io.github.franciscosviana.stmservicos.domain.repository.spec;

import io.github.franciscosviana.stmservicos.domain.model.Credenciado;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CredenciadoSpecification")
class CredenciadoSpecificationTest {

    @Test
    @DisplayName("comFiltros() deve retornar Specification não nula com todos os filtros nulos")
    void deveRetornarSpecificationComFiltrosNulos() {
        Specification<Credenciado> spec = CredenciadoSpecification.comFiltros(
                null, null, null, null, null);

        assertThat(spec).isNotNull();
    }

    @Test
    @DisplayName("comFiltros() deve retornar Specification não nula com código informado")
    void deveRetornarSpecificationComCodigo() {
        Specification<Credenciado> spec = CredenciadoSpecification.comFiltros(
                1L, null, null, null, null);

        assertThat(spec).isNotNull();
    }

    @Test
    @DisplayName("comFiltros() deve retornar Specification não nula com rag informado")
    void deveRetornarSpecificationComRag() {
        Specification<Credenciado> spec = CredenciadoSpecification.comFiltros(
                null, "Empresa X", null, null, null);

        assertThat(spec).isNotNull();
    }

    @Test
    @DisplayName("comFiltros() deve retornar Specification não nula com numeroPessoa informado")
    void deveRetornarSpecificationComNumeroPessoa() {
        Specification<Credenciado> spec = CredenciadoSpecification.comFiltros(
                null, null, "12.345.678/0001-00", null, null);

        assertThat(spec).isNotNull();
    }

    @Test
    @DisplayName("comFiltros() deve retornar Specification não nula com cidade informada")
    void deveRetornarSpecificationComCidade() {
        Specification<Credenciado> spec = CredenciadoSpecification.comFiltros(
                null, null, null, "Teresina", null);

        assertThat(spec).isNotNull();
    }

    @Test
    @DisplayName("comFiltros() deve retornar Specification não nula com estado informado")
    void deveRetornarSpecificationComEstado() {
        Specification<Credenciado> spec = CredenciadoSpecification.comFiltros(
                null, null, null, null, "PI");

        assertThat(spec).isNotNull();
    }

    @Test
    @DisplayName("comFiltros() deve retornar Specification não nula com todos os filtros preenchidos")
    void deveRetornarSpecificationComTodosFiltros() {
        Specification<Credenciado> spec = CredenciadoSpecification.comFiltros(
                1L, "Empresa X", "12.345.678/0001-00", "Teresina", "PI");

        assertThat(spec).isNotNull();
    }

    @Test
    @DisplayName("comFiltros() deve ignorar rag em branco")
    void deveIgnorarRagEmBranco() {
        Specification<Credenciado> spec = CredenciadoSpecification.comFiltros(
                null, "   ", null, null, null);

        assertThat(spec).isNotNull();
    }

    @Test
    @DisplayName("comFiltros() deve ignorar cidade em branco")
    void deveIgnorarCidadeEmBranco() {
        Specification<Credenciado> spec = CredenciadoSpecification.comFiltros(
                null, null, null, "", null);

        assertThat(spec).isNotNull();
    }

    @Test
    @DisplayName("comFiltros() deve ignorar estado em branco")
    void deveIgnorarEstadoEmBranco() {
        Specification<Credenciado> spec = CredenciadoSpecification.comFiltros(
                null, null, null, null, "");

        assertThat(spec).isNotNull();
    }
}