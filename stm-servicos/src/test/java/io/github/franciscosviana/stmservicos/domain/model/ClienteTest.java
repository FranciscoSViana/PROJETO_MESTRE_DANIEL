package io.github.franciscosviana.stmservicos.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Cliente")
class ClienteTest {

    @Nested
    @DisplayName("builder")
    class Builder {

        @Test
        @DisplayName("deve ter lista de contratos vazia por padrão")
        void contratosVaziosPorPadrao() {
            Cliente c = Cliente.builder()
                    .id(UUID.randomUUID())
                    .nome("Empresa X")
                    .build();

            assertThat(c.getContratos()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("deve criar cliente com dados completos")
        void builderCompleto() {
            UUID id = UUID.randomUUID();
            Cliente c = Cliente.builder()
                    .id(id)
                    .nome("Empresa X")
                    .razaoSocial("EMPRESA X LTDA")
                    .cnpj("12.345.678/0001-90")
                    .valorChamado(new BigDecimal("200.00"))
                    .valorKm(new BigDecimal("3.00"))
                    .build();

            assertThat(c.getId()).isEqualTo(id);
            assertThat(c.getNome()).isEqualTo("Empresa X");
            assertThat(c.getRazaoSocial()).isEqualTo("EMPRESA X LTDA");
            assertThat(c.getCnpj()).isEqualTo("12.345.678/0001-90");
            assertThat(c.getValorChamado()).isEqualByComparingTo("200.00");
        }
    }

    @Nested
    @DisplayName("setters")
    class Setters {

        @Test
        @DisplayName("setRazaoSocial deve atualizar o campo")
        void setRazaoSocial() {
            Cliente c = new Cliente();
            c.setRazaoSocial("EMPRESA X LTDA");
            assertThat(c.getRazaoSocial()).isEqualTo("EMPRESA X LTDA");
        }

        @Test
        @DisplayName("setCnpj deve atualizar o campo")
        void setCnpj() {
            Cliente c = new Cliente();
            c.setCnpj("12.345.678/0001-90");
            assertThat(c.getCnpj()).isEqualTo("12.345.678/0001-90");
        }
    }
}
