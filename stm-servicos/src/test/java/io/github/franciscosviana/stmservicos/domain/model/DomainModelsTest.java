package io.github.franciscosviana.stmservicos.domain.model;

import io.github.franciscosviana.stmservicos.api.model.input.EnderecoInput;
import io.github.franciscosviana.stmservicos.domain.model.enums.TipoFluxoPagamento;
import io.github.franciscosviana.stmservicos.domain.model.enums.TipoPessoa;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Domain Models")
class DomainModelsTest {

    // ──────────────────────────────────────────────────────────────────────────
    // Usuario
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Usuario")
    class UsuarioTest {

        @Test
        @DisplayName("getIdade() deve calcular corretamente a partir da data de nascimento")
        void deveCalcularIdade() {
            LocalDate nascimento = LocalDate.now().minusYears(30);
            Usuario u = Usuario.builder()
                    .username("joao").nome("joao").email("j@e.com").senha("h")
                    .roles(Set.of("USER")).dataNascimento(nascimento)
                    .createdAt(Instant.now()).ultimaAlteracaoSenha(Instant.now()).build();

            assertThat(u.getIdade()).isEqualTo(30);
        }

        @Test
        @DisplayName("getIdade() deve retornar null quando dataNascimento é nula")
        void deveRetornarNullSemNascimento() {
            Usuario u = Usuario.builder()
                    .username("joao").nome("joao").email("j@e.com").senha("h")
                    .roles(Set.of("USER")).dataNascimento(null)
                    .createdAt(Instant.now()).ultimaAlteracaoSenha(Instant.now()).build();

            assertThat(u.getIdade()).isNull();
        }

        @Test
        @DisplayName("enabled deve ser true por padrão no builder")
        void enabledPadrao() {
            Usuario u = new Usuario();
            // usando new (campo primitivo boolean padrão false)
            assertThat(u.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("notificacaoSenhaEnviada deve ser false por padrão")
        void notificacaoPadrao() {
            Usuario u = new Usuario();
            assertThat(u.isNotificacaoSenhaEnviada()).isFalse();
        }

        @Test
        @DisplayName("builder deve criar usuário com todos os campos")
        void builderCompleto() {
            UUID id = UUID.randomUUID();
            Usuario u = Usuario.builder()
                    .id(id)
                    .nomeCompleto("João Silva")
                    .username("joao.silva")
                    .nome("joao.silva")
                    .email("joao@email.com")
                    .senha("hash")
                    .roles(Set.of("USER", "ADMIN"))
                    .enabled(true)
                    .createdAt(Instant.now())
                    .ultimaAlteracaoSenha(Instant.now())
                    .build();

            assertThat(u.getId()).isEqualTo(id);
            assertThat(u.getNomeCompleto()).isEqualTo("João Silva");
            assertThat(u.getRoles()).containsExactlyInAnyOrder("USER", "ADMIN");
            assertThat(u.isEnabled()).isTrue();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Endereco
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Endereco")
    class EnderecoTest {

        @Test
        @DisplayName("from(EnderecoInput) deve mapear todos os campos")
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
        @DisplayName("from(null) deve retornar null")
        void deveRetornarNullParaInputNulo() {
            assertThat(Endereco.from(null)).isNull();
        }

        @Test
        @DisplayName("builder deve criar Endereco com campos corretos")
        void builderFunciona() {
            Endereco e = Endereco.builder()
                    .cep("64000-000").cidade("Teresina").estado("PI").build();

            assertThat(e.getCep()).isEqualTo("64000-000");
            assertThat(e.getCidade()).isEqualTo("Teresina");
            assertThat(e.getEstado()).isEqualTo("PI");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GeoLocation
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GeoLocation")
    class GeoLocationTest {

        @Test
        @DisplayName("deve criar GeoLocation com latitude e longitude")
        void deveCriar() {
            GeoLocation geo = new GeoLocation(-5.0919, -42.8034);
            assertThat(geo.getLatitude()).isEqualTo(-5.0919);
            assertThat(geo.getLongitude()).isEqualTo(-42.8034);
        }

        @Test
        @DisplayName("equals deve funcionar para mesmas coordenadas")
        void equalsCoordenadasIguais() {
            GeoLocation g1 = new GeoLocation(-5.0, -42.0);
            GeoLocation g2 = new GeoLocation(-5.0, -42.0);
            assertThat(g1).isEqualTo(g2);
        }

        @Test
        @DisplayName("equals deve diferenciar coordenadas distintas")
        void equalsCoordenadasDiferentes() {
            GeoLocation g1 = new GeoLocation(-5.0, -42.0);
            GeoLocation g2 = new GeoLocation(-6.0, -43.0);
            assertThat(g1).isNotEqualTo(g2);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Credenciado
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Credenciado")
    class CredenciadoTest {

        @Test
        @DisplayName("builder deve criar credenciado com dados completos")
        void builderCompleto() {
            UUID id = UUID.randomUUID();
            GeoLocation geo = new GeoLocation(-5.0, -42.0);
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
            assertThat(c.getValorChamado()).isEqualByComparingTo("120.00");
            assertThat(c.getGeoLocation()).isEqualTo(geo);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Cliente
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Cliente")
    class ClienteTest {

        @Test
        @DisplayName("novo cliente deve ter lista de contratos vazia por padrão (builder.default)")
        void contratosVaziosPorPadrao() {
            Cliente c = Cliente.builder()
                    .id(UUID.randomUUID())
                    .nome("Empresa X")
                    .build();

            assertThat(c.getContratos()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("setRazaoSocial deve atualizar o campo")
        void setRazaoSocial() {
            Cliente c = new Cliente();
            c.setRazaoSocial("EMPRESA X LTDA");
            assertThat(c.getRazaoSocial()).isEqualTo("EMPRESA X LTDA");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // RefreshToken
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("RefreshToken")
    class RefreshTokenTest {

        @Test
        @DisplayName("builder deve criar RefreshToken com token e expiração")
        void builderFunciona() {
            Instant expiry = Instant.now().plusSeconds(3600);
            RefreshToken t = RefreshToken.builder()
                    .token("abc-123")
                    .expiryDate(expiry)
                    .build();

            assertThat(t.getToken()).isEqualTo("abc-123");
            assertThat(t.getExpiryDate()).isEqualTo(expiry);
        }
    }
}
