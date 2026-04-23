package io.github.franciscosviana.stmservicos.domain.model.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Enums do domínio")
class EnumsTest {

    // ──────────────────────────────────────────────────────────────────────────
    // TipoPessoa
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("TipoPessoa")
    class TipoPessoaTest {

        @Test
        @DisplayName("fromCodigo(1) deve retornar PESSOA_FISICA")
        void deveRetornarPessoaFisica() {
            assertThat(TipoPessoa.fromCodigo(1)).isEqualTo(TipoPessoa.PESSOA_FISICA);
        }

        @Test
        @DisplayName("fromCodigo(2) deve retornar PESSOA_JURIDICA")
        void deveRetornarPessoaJuridica() {
            assertThat(TipoPessoa.fromCodigo(2)).isEqualTo(TipoPessoa.PESSOA_JURIDICA);
        }

        @Test
        @DisplayName("fromCodigo com código inválido deve lançar IllegalArgumentException")
        void deveLancarExcecaoCodigoInvalido() {
            assertThatThrownBy(() -> TipoPessoa.fromCodigo(99))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("PESSOA_FISICA deve ter código 1 e descrição 'Pessoa física'")
        void pessoaFisicaDeveTermetadados() {
            assertThat(TipoPessoa.PESSOA_FISICA.getCodigo()).isEqualTo(1);
            assertThat(TipoPessoa.PESSOA_FISICA.getDescricao()).isEqualTo("Pessoa física");
        }

        @Test
        @DisplayName("PESSOA_JURIDICA deve ter código 2 e descrição 'Pessoa jurídica'")
        void pessoaJuridicaDeveTermetadados() {
            assertThat(TipoPessoa.PESSOA_JURIDICA.getCodigo()).isEqualTo(2);
            assertThat(TipoPessoa.PESSOA_JURIDICA.getDescricao()).isEqualTo("Pessoa jurídica");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // StatusRastreio
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("StatusRastreio")
    class StatusRastreioTest {

        @Test
        @DisplayName("deve ter 5 valores definidos")
        void deveTerCincoValores() {
            assertThat(StatusRastreio.values()).hasSize(6);
        }

        @ParameterizedTest(name = "{0} deve ter descrição e cor não nulos")
        @CsvSource({
                "POSTADO", "A_CAMINHO", "CHEGOU", "DEVOLVIDO", "AGUARDANDO"
        })
        @DisplayName("todos os status devem ter descrição e cor")
        void todosDeveTermetadados(String nome) {
            StatusRastreio s = StatusRastreio.valueOf(nome);
            assertThat(s.getDescricao()).isNotBlank();
            assertThat(s.getCor()).isNotBlank();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // StatusOrdem
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("StatusOrdem")
    class StatusOrdemTest {

        @Test
        @DisplayName("deve conter ABERTA e CONCLUIDA")
        void deveConterValoresEssenciais() {
            assertThat(StatusOrdem.values())
                    .contains(StatusOrdem.ABERTA, StatusOrdem.CONCLUIDA);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // TipoAcaoOS
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("TipoAcaoOS")
    class TipoAcaoOSTest {

        @Test
        @DisplayName("deve conter CRIACAO, ATUALIZACAO, MUDANCA_STATUS e CONCLUSAO_ORDEM")
        void deveConterAcoesEssenciais() {
            assertThat(TipoAcaoOS.values())
                    .contains(
                            TipoAcaoOS.CRIACAO,
                            TipoAcaoOS.ATUALIZACAO,
                            TipoAcaoOS.MUDANCA_STATUS,
                            TipoAcaoOS.CONCLUSAO_ORDEM
                    );
        }
    }
}
