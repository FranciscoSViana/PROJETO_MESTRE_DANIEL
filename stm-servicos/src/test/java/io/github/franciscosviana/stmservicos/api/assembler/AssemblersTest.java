package io.github.franciscosviana.stmservicos.api.assembler;

import io.github.franciscosviana.stmservicos.api.model.input.CredenciadoInput;
import io.github.franciscosviana.stmservicos.api.model.input.OrdemServicoInput;
import io.github.franciscosviana.stmservicos.api.model.output.*;
import io.github.franciscosviana.stmservicos.domain.model.*;
import io.github.franciscosviana.stmservicos.domain.model.enums.StatusOrdem;
import io.github.franciscosviana.stmservicos.domain.model.enums.StatusRastreio;
import io.github.franciscosviana.stmservicos.domain.model.enums.TipoAcaoOS;
import io.github.franciscosviana.stmservicos.domain.model.enums.TipoFluxoPagamento;
import io.github.franciscosviana.stmservicos.domain.model.enums.TipoPagamento;
import io.github.franciscosviana.stmservicos.domain.model.enums.TipoPessoa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Assemblers")
class AssemblersTest {

    // ──────────────────────────────────────────────────────────────────────────
    // ClienteOutputAssembler
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("ClienteOutputAssembler")
    class ClienteOutputAssemblerTest {

        @Mock ContratoOutputAssembler contratoAssembler;
        @InjectMocks ClienteOutputAssembler assembler;

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

    // ──────────────────────────────────────────────────────────────────────────
    // OrdemServicoOutputAssembler
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("OrdemServicoOutputAssembler")
    class OrdemServicoOutputAssemblerTest {

        @Mock TecnicoOutputAssembler tecnicoAssembler;
        @Mock ClienteOutputAssembler clienteOutputAssembler;
        @Mock CredenciadoOutputAssembler credenciadoAssembler;
        @Mock ContratoOutputAssembler contratoOutputAssembler;

        @InjectMocks OrdemServicoOutputAssembler assembler;

        @Test
        @DisplayName("toModel() deve retornar null quando OS é null")
        void deveRetornarNullSeNull() {
            assertThat(assembler.toModel(null)).isNull();
        }

        @Test
        @DisplayName("toModel() deve mapear campos simples da OS")
        void deveMappearCamposSimples() {
            UUID osId = UUID.randomUUID();
            OrdemServico os = new OrdemServico();
            os.setId(osId);
            os.setOsg("OSG260001");
            os.setOsClt("CLT-001");
            os.setStatus(StatusOrdem.ABERTA);
            os.setContato("Contato X");
            os.setEquipamento("Notebook");
            os.setDefeito("Não liga");

            when(clienteOutputAssembler.toModel(null)).thenReturn(null);
            when(tecnicoAssembler.toModel(null)).thenReturn(null);

            OrdemServicoOutput output = assembler.toModel(os);

            assertThat(output.getId()).isEqualTo(osId);
            assertThat(output.getOsg()).isEqualTo("OSG260001");
            assertThat(output.getOsClt()).isEqualTo("CLT-001");
            assertThat(output.getStatus()).isEqualTo("ABERTA");
            assertThat(output.getContato()).isEqualTo("Contato X");
            assertThat(output.getEquipamento()).isEqualTo("Notebook");
            assertThat(output.getDefeito()).isEqualTo("Não liga");
        }

        @Test
        @DisplayName("toModel() deve mapear statusRastreio quando presente")
        void deveMappearStatusRastreio() {
            OrdemServico os = new OrdemServico();
            os.setStatusRastreio(StatusRastreio.CHEGOU);

            when(clienteOutputAssembler.toModel(null)).thenReturn(null);
            when(tecnicoAssembler.toModel(null)).thenReturn(null);

            OrdemServicoOutput output = assembler.toModel(os);

            assertThat(output.getStatusRastreio()).isEqualTo("CHEGOU");
            assertThat(output.getStatusRastreioDescricao()).isEqualTo("Chegou");
            assertThat(output.getStatusRasteioCor()).isNotNull();
        }

        @Test
        @DisplayName("toModel() deve retornar statusRastreio null quando não informado")
        void deveRetornarStatusRastreioNullQuandoNaoInformado() {
            OrdemServico os = new OrdemServico();
            os.setStatusRastreio(null);

            when(clienteOutputAssembler.toModel(null)).thenReturn(null);
            when(tecnicoAssembler.toModel(null)).thenReturn(null);

            OrdemServicoOutput output = assembler.toModel(os);

            assertThat(output.getStatusRastreio()).isNull();
            assertThat(output.getStatusRastreioDescricao()).isNull();
        }

        @Test
        @DisplayName("toModel() deve mapear credenciado quando presente")
        void deveMappearCredenciado() {
            Credenciado credenciado = new Credenciado();
            credenciado.setId(UUID.randomUUID());
            CredenciadoOutput credenciadoOutput = new CredenciadoOutput();

            OrdemServico os = new OrdemServico();
            os.setCredenciado(credenciado);

            when(clienteOutputAssembler.toModel(null)).thenReturn(null);
            when(tecnicoAssembler.toModel(null)).thenReturn(null);
            when(credenciadoAssembler.toModel(credenciado)).thenReturn(credenciadoOutput);

            OrdemServicoOutput output = assembler.toModel(os);

            assertThat(output.getCredenciado()).isEqualTo(credenciadoOutput);
        }

        @Test
        @DisplayName("toModel() deve retornar credenciado null quando não presente")
        void deveRetornarCredenciadoNullQuandoAusente() {
            OrdemServico os = new OrdemServico();
            os.setCredenciado(null);

            when(clienteOutputAssembler.toModel(null)).thenReturn(null);
            when(tecnicoAssembler.toModel(null)).thenReturn(null);

            OrdemServicoOutput output = assembler.toModel(os);

            assertThat(output.getCredenciado()).isNull();
        }

        @Test
        @DisplayName("toModel() deve mapear endereço quando presente")
        void deveMappearEndereco() {
            OrdemServico os = new OrdemServico();
            os.setEndereco(Endereco.builder()
                    .cep("64000-000").logradouro("Rua A").bairro("Centro")
                    .numero("100").cidade("Teresina").estado("PI").build());

            when(clienteOutputAssembler.toModel(null)).thenReturn(null);
            when(tecnicoAssembler.toModel(null)).thenReturn(null);

            OrdemServicoOutput output = assembler.toModel(os);

            assertThat(output.getEndereco()).isNotNull();
            assertThat(output.getEndereco().getCep()).isEqualTo("64000-000");
        }

        @Test
        @DisplayName("toModel() deve retornar endereco null quando não presente")
        void deveRetornarEnderecoNullQuandoAusente() {
            OrdemServico os = new OrdemServico();
            os.setEndereco(null);

            when(clienteOutputAssembler.toModel(null)).thenReturn(null);
            when(tecnicoAssembler.toModel(null)).thenReturn(null);

            OrdemServicoOutput output = assembler.toModel(os);

            assertThat(output.getEndereco()).isNull();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // OrdemServicoInputDisassembler
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("OrdemServicoInputDisassembler")
    class OrdemServicoInputDisassemblerTest {

        private final OrdemServicoInputDisassembler disassembler = new OrdemServicoInputDisassembler();

        @Test
        @DisplayName("toDomainObject() deve retornar null quando input é null")
        void deveRetornarNullSeNull() {
            assertThat(disassembler.toDomainObject(null)).isNull();
        }

        @Test
        @DisplayName("toDomainObject() deve mapear campos básicos")
        void deveMappearCamposBasicos() {
            OrdemServicoInput input = new OrdemServicoInput();
            input.setOsClt("CLT-001");
            input.setOsg("OSG260001");
            input.setContato("Contato X");
            input.setDepartamento("TI");
            input.setTelefone("86999999999");
            input.setAcionador("Gerente");
            input.setEquipamento("Notebook");
            input.setSerie("SN12345");
            input.setPib("123");
            input.setDefeito("Não liga");
            input.setRastreio("XYZ123");

            OrdemServico os = disassembler.toDomainObject(input);

            assertThat(os.getOsClt()).isEqualTo("CLT-001");
            assertThat(os.getOsg()).isEqualTo("OSG260001");
            assertThat(os.getContato()).isEqualTo("Contato X");
            assertThat(os.getDepartamento()).isEqualTo("TI");
            assertThat(os.getTelefone()).isEqualTo("86999999999");
            assertThat(os.getEquipamento()).isEqualTo("Notebook");
        }

        @Test
        @DisplayName("toDomainObject() deve mapear status quando informado")
        void deveMappearStatus() {
            OrdemServicoInput input = new OrdemServicoInput();
            input.setStatus("CONCLUIDA");

            OrdemServico os = disassembler.toDomainObject(input);

            assertThat(os.getStatus()).isEqualTo(StatusOrdem.CONCLUIDA);
        }

        @Test
        @DisplayName("toDomainObject() deve usar data atual quando dataHoraAbertura não informada")
        void deveUsarDataAtualQuandoNaoInformada() {
            OrdemServicoInput input = new OrdemServicoInput();
            input.setDataHoraAbertura(null);

            OffsetDateTime antes = OffsetDateTime.now().minusSeconds(1);
            OrdemServico os = disassembler.toDomainObject(input);
            OffsetDateTime depois = OffsetDateTime.now().plusSeconds(1);

            assertThat(os.getDataHoraAbertura()).isBetween(antes, depois);
        }

        @Test
        @DisplayName("toDomainObject() deve usar data informada quando presente")
        void deveUsarDataInformada() {
            OffsetDateTime data = OffsetDateTime.now().minusDays(1);
            OrdemServicoInput input = new OrdemServicoInput();
            input.setDataHoraAbertura(data);

            OrdemServico os = disassembler.toDomainObject(input);

            assertThat(os.getDataHoraAbertura()).isEqualTo(data);
        }

        @Test
        @DisplayName("toDomainObject() deve criar entidade cliente com id quando clienteId informado")
        void deveMappearClienteId() {
            UUID clienteId = UUID.randomUUID();
            OrdemServicoInput input = new OrdemServicoInput();
            input.setClienteId(clienteId);

            OrdemServico os = disassembler.toDomainObject(input);

            assertThat(os.getCliente()).isNotNull();
            assertThat(os.getCliente().getId()).isEqualTo(clienteId);
        }

        @Test
        @DisplayName("toDomainObject() deve mapear statusRastreio quando informado")
        void deveMappearStatusRastreio() {
            OrdemServicoInput input = new OrdemServicoInput();
            input.setStatusRastreio("CHEGOU");

            OrdemServico os = disassembler.toDomainObject(input);

            assertThat(os.getStatusRastreio()).isEqualTo(StatusRastreio.CHEGOU);
        }

        @Test
        @DisplayName("toDomainObject() deve ignorar statusRastreio quando em branco")
        void deveIgnorarStatusRastreioEmBranco() {
            OrdemServicoInput input = new OrdemServicoInput();
            input.setStatusRastreio("  ");

            OrdemServico os = disassembler.toDomainObject(input);

            assertThat(os.getStatusRastreio()).isNull();
        }

        @Test
        @DisplayName("copyToDomainObject() deve copiar todos os campos para a entidade existente")
        void deveCopiarParaEntidadeExistente() {
            OrdemServico osExistente = new OrdemServico();
            osExistente.setOsClt("ANTIGO");
            osExistente.setDefeito("Defeito antigo");

            OrdemServicoInput input = new OrdemServicoInput();
            input.setOsClt("NOVO");
            input.setDefeito("Defeito novo");

            disassembler.copyToDomainObject(input, osExistente);

            assertThat(osExistente.getOsClt()).isEqualTo("NOVO");
            assertThat(osExistente.getDefeito()).isEqualTo("Defeito novo");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // HistoricoOrdemServicoOutputAssembler
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("HistoricoOrdemServicoOutputAssembler")
    class HistoricoOrdemServicoOutputAssemblerTest {

        @Mock ModelMapper modelMapper;
        @InjectMocks HistoricoOrdemServicoOutputAssembler assembler;

        @Test
        @DisplayName("toModel() deve retornar null quando historico é null")
        void deveRetornarNullSeNull() {
            assertThat(assembler.toModel(null)).isNull();
        }

        @Test
        @DisplayName("toModel() deve mapear campos e definir ordemServicoId e acao")
        void deveMappearHistorico() {
            UUID osId = UUID.randomUUID();

            OrdemServico os = new OrdemServico();
            os.setId(osId);

            Usuario usuario = new Usuario();

            HistoricoOrdemServico historico = HistoricoOrdemServico.builder()
                    .ordemServico(os)
                    .usuario(usuario)
                    .acao(TipoAcaoOS.CRIACAO)
                    .descricao("OS criada")
                    .dataHora(OffsetDateTime.now())
                    .build();

            HistoricoOrdemServicoOutput output = new HistoricoOrdemServicoOutput();
            UsuarioResumoOutput usuarioOutput = new UsuarioResumoOutput();

            when(modelMapper.map(historico, HistoricoOrdemServicoOutput.class)).thenReturn(output);
            when(modelMapper.map(usuario, UsuarioResumoOutput.class)).thenReturn(usuarioOutput);

            HistoricoOrdemServicoOutput resultado = assembler.toModel(historico);

            assertThat(resultado.getOrdemServicoId()).isEqualTo(osId);
            assertThat(resultado.getAcao()).isEqualTo("CRIACAO");
            assertThat(resultado.getUsuario()).isEqualTo(usuarioOutput);
        }

        @Test
        @DisplayName("toCollectionModel() deve mapear lista de históricos")
        void deveMappearColecao() {
            UUID osId = UUID.randomUUID();
            OrdemServico os = new OrdemServico();
            os.setId(osId);

            HistoricoOrdemServico historico = HistoricoOrdemServico.builder()
                    .ordemServico(os)
                    .usuario(new Usuario())
                    .acao(TipoAcaoOS.ATUALIZACAO)
                    .build();

            HistoricoOrdemServicoOutput output = new HistoricoOrdemServicoOutput();
            when(modelMapper.map(historico, HistoricoOrdemServicoOutput.class)).thenReturn(output);
            when(modelMapper.map(any(Usuario.class), eq(UsuarioResumoOutput.class)))
                    .thenReturn(new UsuarioResumoOutput());

            List<HistoricoOrdemServicoOutput> resultado = assembler.toCollectionModel(List.of(historico));

            assertThat(resultado).hasSize(1);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // PagamentoClienteOSOutputAssembler
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("PagamentoClienteOSOutputAssembler")
    class PagamentoClienteOSOutputAssemblerTest {

        private final PagamentoClienteOSOutputAssembler assembler = new PagamentoClienteOSOutputAssembler();

        @Test
        @DisplayName("toModel() deve mapear todos os campos do pagamento cliente")
        void deveMappearPagamentoCliente() {
            PagamentoClienteOS p = PagamentoClienteOS.builder()
                    .id(1L)
                    .osg("OSG260001")
                    .osClt("CLT-001")
                    .cliente("Empresa X")
                    .contrato("uuid-contrato")
                    .valorChamado(new java.math.BigDecimal("150.00"))
                    .km(new java.math.BigDecimal("10"))
                    .valorKm(new java.math.BigDecimal("2.50"))
                    .pedagio(new java.math.BigDecimal("5.00"))
                    .estacionamento(new java.math.BigDecimal("3.00"))
                    .valorOutros(new java.math.BigDecimal("2.00"))
                    .valorTotal(new java.math.BigDecimal("185.00"))
                    .tipoPagamento(TipoPagamento.PIX)
                    .pago(true)
                    .corrigido(true)
                    .build();

            PagamentoClienteOSOutput out = assembler.toModel(p);

            assertThat(out.getId()).isEqualTo(1L);
            assertThat(out.getOsg()).isEqualTo("OSG260001");
            assertThat(out.getValorTotal()).isEqualByComparingTo("185.00");
            assertThat(out.isPago()).isTrue();
            assertThat(out.isCorrigido()).isTrue();
        }

        @Test
        @DisplayName("toCollectionModel() deve mapear lista de pagamentos cliente")
        void deveMappearColecao() {
            PagamentoClienteOS p = PagamentoClienteOS.builder()
                    .valorChamado(java.math.BigDecimal.ZERO)
                    .km(java.math.BigDecimal.ZERO)
                    .valorKm(java.math.BigDecimal.ZERO)
                    .pedagio(java.math.BigDecimal.ZERO)
                    .estacionamento(java.math.BigDecimal.ZERO)
                    .valorOutros(java.math.BigDecimal.ZERO)
                    .valorTotal(java.math.BigDecimal.ZERO)
                    .build();

            List<PagamentoClienteOSOutput> result = assembler.toCollectionModel(List.of(p));
            assertThat(result).hasSize(1);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // CredenciadoInputDisassembler
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("CredenciadoInputDisassembler")
    class CredenciadoInputDisassemblerTest {

        @Mock ModelMapper modelMapper;
        @InjectMocks CredenciadoInputDisassembler disassembler;

        @Test
        @DisplayName("toDomainObject() deve mapear tipoPessoa PESSOA_FISICA (código 1)")
        void deveMappearTipoPessoaFisica() {
            CredenciadoInput input = new CredenciadoInput();
            input.setTipoPessoa(1);
            input.setRag("Pessoa Física");

            Credenciado credenciado = new Credenciado();
            when(modelMapper.map(input, Credenciado.class)).thenReturn(credenciado);

            Credenciado resultado = disassembler.toDomainObject(input);

            assertThat(resultado.getTipoPessoa()).isEqualTo(TipoPessoa.PESSOA_FISICA);
        }

        @Test
        @DisplayName("toDomainObject() deve mapear tipoPessoa PESSOA_JURIDICA (código 2)")
        void deveMappearTipoPessoaJuridica() {
            CredenciadoInput input = new CredenciadoInput();
            input.setTipoPessoa(2);

            Credenciado credenciado = new Credenciado();
            when(modelMapper.map(input, Credenciado.class)).thenReturn(credenciado);

            Credenciado resultado = disassembler.toDomainObject(input);

            assertThat(resultado.getTipoPessoa()).isEqualTo(TipoPessoa.PESSOA_JURIDICA);
        }

        @Test
        @DisplayName("toDomainObject() deve mapear tipoFluxoPagamento quando informado")
        void deveMappearTipoFluxoPagamento() {
            CredenciadoInput input = new CredenciadoInput();
            input.setTipoPessoa(1);
            input.setTipoFluxoPagamento("MENSAL");

            Credenciado credenciado = new Credenciado();
            when(modelMapper.map(input, Credenciado.class)).thenReturn(credenciado);

            Credenciado resultado = disassembler.toDomainObject(input);

            assertThat(resultado.getTipoFluxoPagamento()).isEqualTo(TipoFluxoPagamento.MENSAL);
        }

        @Test
        @DisplayName("toDomainObject() não deve mapear tipoFluxoPagamento quando não informado")
        void naoDeveMappearTipoFluxoPagamentoNulo() {
            CredenciadoInput input = new CredenciadoInput();
            input.setTipoPessoa(1);
            input.setTipoFluxoPagamento(null);

            Credenciado credenciado = new Credenciado();
            when(modelMapper.map(input, Credenciado.class)).thenReturn(credenciado);

            Credenciado resultado = disassembler.toDomainObject(input);

            assertThat(resultado.getTipoFluxoPagamento()).isNull();
        }

        @Test
        @DisplayName("copyToDomainObject() deve copiar tipoPessoa e tipoFluxoPagamento")
        void deveCopiarTipoPessoaETipoFluxo() {
            CredenciadoInput input = new CredenciadoInput();
            input.setTipoPessoa(2);
            input.setTipoFluxoPagamento("QUINZENAL");

            Credenciado credenciado = new Credenciado();

            disassembler.copyToDomainObject(input, credenciado);

            assertThat(credenciado.getTipoPessoa()).isEqualTo(TipoPessoa.PESSOA_JURIDICA);
            assertThat(credenciado.getTipoFluxoPagamento()).isEqualTo(TipoFluxoPagamento.QUINZENAL);
        }
    }
}