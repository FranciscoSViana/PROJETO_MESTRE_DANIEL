package io.github.franciscosviana.stmservicos.api.assembler;

import io.github.franciscosviana.stmservicos.api.model.output.ClienteOutput;
import io.github.franciscosviana.stmservicos.api.model.output.ContratoOutput;
import io.github.franciscosviana.stmservicos.api.model.output.PagamentoOSOutput;
import io.github.franciscosviana.stmservicos.domain.model.Cliente;
import io.github.franciscosviana.stmservicos.domain.model.Contrato;
import io.github.franciscosviana.stmservicos.domain.model.Endereco;
import io.github.franciscosviana.stmservicos.domain.model.PagamentoOS;
import io.github.franciscosviana.stmservicos.domain.model.enums.TipoPagamento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Assemblers")
class AssemblersTest {

    // ──────────────────────────────────────────────────────────────────────────
    // ClienteOutputAssembler
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("ClienteOutputAssembler")
    class ClienteOutputAssemblerTest {

        @Mock
        ContratoOutputAssembler contratoAssembler;
        @InjectMocks
        ClienteOutputAssembler assembler;

        private Cliente cliente;

        @BeforeEach
        void setUp() {
            Endereco endereco = Endereco.builder()
                    .cep("64000-000").logradouro("Av. A").bairro("Centro")
                    .numero("1").cidade("Teresina").estado("PI").build();

            cliente = new Cliente();
            cliente.setId(UUID.randomUUID());
            cliente.setCodigo(1L);
            cliente.setNome("Empresa X");
            cliente.setRazaoSocial("EMPRESA X LTDA");
            cliente.setCnpj("12.345.678/0001-90");
            cliente.setValorChamado(new BigDecimal("100.00"));
            cliente.setValorKm(new BigDecimal("2.50"));
            cliente.setEndereco(endereco);
            cliente.setContratos(List.of());
        }

        @Test
        @DisplayName("toModel() deve mapear todos os campos do cliente")
        void deveMappearCliente() {
            ClienteOutput output = assembler.toModel(cliente);

            assertThat(output.getId()).isEqualTo(cliente.getId());
            assertThat(output.getCodigo()).isEqualTo(1L);
            assertThat(output.getNome()).isEqualTo("Empresa X");
            assertThat(output.getRazaoSocial()).isEqualTo("EMPRESA X LTDA");
            assertThat(output.getCnpj()).isEqualTo("12.345.678/0001-90");
            assertThat(output.getValorChamado()).isEqualByComparingTo("100.00");
            assertThat(output.getValorKm()).isEqualByComparingTo("2.50");
            assertThat(output.getEndereco()).isNotNull();
            assertThat(output.getEndereco().getCep()).isEqualTo("64000-000");
        }

        @Test
        @DisplayName("toModel() com contratos deve mapear lista de contratos")
        void deveMappearContratos() {
            Contrato contrato = new Contrato();
            contrato.setId(UUID.randomUUID());
            cliente.setContratos(List.of(contrato));

            ContratoOutput contratoOutput = mock(ContratoOutput.class);
            when(contratoAssembler.toModel(contrato)).thenReturn(contratoOutput);

            ClienteOutput output = assembler.toModel(cliente);

            assertThat(output.getContratos()).hasSize(1);
        }

        @Test
        @DisplayName("toModel() com contratos nulos deve retornar lista vazia")
        void deveTratarContratosNulos() {
            cliente.setContratos(null);

            ClienteOutput output = assembler.toModel(cliente);

            assertThat(output.getContratos()).isEmpty();
        }

        @Test
        @DisplayName("toCollectionModel() deve mapear lista de clientes")
        void deveMappearColecao() {
            List<ClienteOutput> result = assembler.toCollectionModel(List.of(cliente));
            assertThat(result).hasSize(1);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // PagamentoOSOutputAssembler
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("PagamentoOSOutputAssembler")
    class PagamentoOSOutputAssemblerTest {

        private final PagamentoOSOutputAssembler assembler = new PagamentoOSOutputAssembler();

        @Test
        @DisplayName("toModel() deve mapear todos os campos do pagamento")
        void deveMappearPagamento() {
            PagamentoOS p = PagamentoOS.builder()
                    .id(1L)
                    .osg("OSG260001")
                    .osClt("CLT-001")
                    .cliente("Empresa X")
                    .contrato("uuid-contrato")
                    .valorChamado(new BigDecimal("100.00"))
                    .km(new BigDecimal("10"))
                    .valorKm(new BigDecimal("2.50"))
                    .pedagio(new BigDecimal("5.00"))
                    .estacionamento(new BigDecimal("3.00"))
                    .valorOutros(new BigDecimal("2.00"))
                    .valorTotal(new BigDecimal("135.00"))
                    .tipoPagamento(TipoPagamento.PIX)
                    .chavePix("chave@pix.com")
                    .pago(true)
                    .dataPagamento(OffsetDateTime.now())
                    .build();

            PagamentoOSOutput out = assembler.toModel(p);

            assertThat(out.getId()).isEqualTo(1L);
            assertThat(out.getOsg()).isEqualTo("OSG260001");
            assertThat(out.getOsClt()).isEqualTo("CLT-001");
            assertThat(out.getValorTotal()).isEqualByComparingTo("135.00");
            assertThat(out.getTipoPagamento()).isEqualTo(TipoPagamento.PIX);
            assertThat(out.getChavePix()).isEqualTo("chave@pix.com");
            assertThat(out.isPago()).isTrue();
        }

        @Test
        @DisplayName("toCollectionModel() deve mapear lista de pagamentos")
        void deveMappearColecao() {
            PagamentoOS p = PagamentoOS.builder()
                    .valorChamado(BigDecimal.ZERO).km(BigDecimal.ZERO)
                    .valorKm(BigDecimal.ZERO).pedagio(BigDecimal.ZERO)
                    .estacionamento(BigDecimal.ZERO).valorOutros(BigDecimal.ZERO)
                    .valorTotal(BigDecimal.ZERO).build();

            List<PagamentoOSOutput> result = assembler.toCollectionModel(List.of(p));
            assertThat(result).hasSize(1);
        }
    }
}
