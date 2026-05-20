package io.github.franciscosviana.stmservicos.domain.model;

import io.github.franciscosviana.stmservicos.api.model.input.EnderecoInput;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Endereco")
class EnderecoTest {

    @Nested
    @DisplayName("from(EnderecoInput)")
    class From {

        @Test
        @DisplayName("deve mapear todos os campos do input")
        void deveMappearDoInput() {
            EnderecoInput input = new EnderecoInput();
            input.setCep("64000-000");
            input.setLogradouro("Av. Principal");
            input.setNumero("100");
            input.setComplemento("Ap 1");
            input.setBairro("Centro");
            input.setCidade("Teresina");
            input.setEstado("PI");

            Endereco endereco = Endereco.from(input);

            assertThat(endereco.getCep()).isEqualTo("64000-000");
            assertThat(endereco.getLogradouro()).isEqualTo("Av. Principal");
            assertThat(endereco.getNumero()).isEqualTo("100");
            assertThat(endereco.getComplemento()).isEqualTo("Ap 1");
            assertThat(endereco.getBairro()).isEqualTo("Centro");
            assertThat(endereco.getCidade()).isEqualTo("Teresina");
            assertThat(endereco.getEstado()).isEqualTo("PI");
        }

        @Test
        @DisplayName("deve retornar null para input nulo")
        void deveRetornarNullParaInputNulo() {
            assertThat(Endereco.from(null)).isNull();
        }
    }

    @Nested
    @DisplayName("builder")
    class Builder {

        @Test
        @DisplayName("deve criar Endereco com campos informados")
        void builderFunciona() {
            Endereco e = Endereco.builder()
                    .cep("64000-000").cidade("Teresina").estado("PI").build();

            assertThat(e.getCep()).isEqualTo("64000-000");
            assertThat(e.getCidade()).isEqualTo("Teresina");
            assertThat(e.getEstado()).isEqualTo("PI");
        }

        @Test
        @DisplayName("deve criar Endereco completo via builder")
        void builderCompleto() {
            Endereco e = Endereco.builder()
                    .cep("64000-000").logradouro("Av. Frei Serafim").numero("1000")
                    .complemento("Sala 5").bairro("Centro").cidade("Teresina").estado("PI")
                    .build();

            assertThat(e.getLogradouro()).isEqualTo("Av. Frei Serafim");
            assertThat(e.getNumero()).isEqualTo("1000");
            assertThat(e.getComplemento()).isEqualTo("Sala 5");
        }
    }
}
