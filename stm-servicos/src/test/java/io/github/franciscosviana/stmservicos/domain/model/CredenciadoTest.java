package io.github.franciscosviana.stmservicos.domain.model;

import io.github.franciscosviana.stmservicos.domain.model.enums.TipoFluxoPagamento;
import io.github.franciscosviana.stmservicos.domain.model.enums.TipoPessoa;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Credenciado")
class CredenciadoTest {

    @Nested
    @DisplayName("builder")
    class Builder {

        @Test
        @DisplayName("deve criar credenciado com dados completos")
        void builderCompleto() {
            UUID id = UUID.randomUUID();
            GeoLocation geo = new GeoLocation(-5.0919, -42.8034);
            Endereco endereco = Endereco.builder().cidade("Teresina").estado("PI").build();

            Credenciado c = Credenciado.builder()
                    .id(id)
                    .codigo(1L)
                    .rag("Credenciado Ltda")
                    .tipoPessoa(TipoPessoa.PESSOA_JURIDICA)
                    .numeroPessoa("12.345.678/0001-90")
                    .valorChamado(new BigDecimal("120.00"))
                    .valorKm(new BigDecimal("2.50"))
                    .tipoFluxoPagamento(TipoFluxoPagamento.MENSAL)
                    .endereco(endereco)
                    .geoLocation(geo)
                    .build();

            assertThat(c.getId()).isEqualTo(id);
            assertThat(c.getRag()).isEqualTo("Credenciado Ltda");
            assertThat(c.getTipoPessoa()).isEqualTo(TipoPessoa.PESSOA_JURIDICA);
            assertThat(c.getNumeroPessoa()).isEqualTo("12.345.678/0001-90");
            assertThat(c.getValorChamado()).isEqualByComparingTo("120.00");
            assertThat(c.getValorKm()).isEqualByComparingTo("2.50");
            assertThat(c.getGeoLocation()).isEqualTo(geo);
            assertThat(c.getEndereco().getCidade()).isEqualTo("Teresina");
        }

        @Test
        @DisplayName("deve aceitar credenciado pessoa física")
        void pessoaFisica() {
            Credenciado c = Credenciado.builder()
                    .tipoPessoa(TipoPessoa.PESSOA_FISICA)
                    .numeroPessoa("123.456.789-00")
                    .build();

            assertThat(c.getTipoPessoa()).isEqualTo(TipoPessoa.PESSOA_FISICA);
        }
    }

    @Nested
    @DisplayName("GeoLocation")
    class GeoLocationTest {

        @Test
        @DisplayName("deve criar com latitude e longitude corretas")
        void deveCriar() {
            GeoLocation geo = new GeoLocation(-5.0919, -42.8034);
            assertThat(geo.getLatitude()).isEqualTo(-5.0919);
            assertThat(geo.getLongitude()).isEqualTo(-42.8034);
        }

        @Test
        @DisplayName("equals deve ser true para mesmas coordenadas")
        void equalsCoordenadasIguais() {
            GeoLocation g1 = new GeoLocation(-5.0, -42.0);
            GeoLocation g2 = new GeoLocation(-5.0, -42.0);
            assertThat(g1).isEqualTo(g2);
        }

        @Test
        @DisplayName("equals deve ser false para coordenadas distintas")
        void equalsCoordenadasDiferentes() {
            GeoLocation g1 = new GeoLocation(-5.0, -42.0);
            GeoLocation g2 = new GeoLocation(-6.0, -43.0);
            assertThat(g1).isNotEqualTo(g2);
        }
    }
}
