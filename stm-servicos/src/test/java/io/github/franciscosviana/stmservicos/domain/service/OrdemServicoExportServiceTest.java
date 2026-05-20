package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.assembler.OrdemServicoOutputAssembler;
import io.github.franciscosviana.stmservicos.api.model.output.OrdemServicoOutput;
import io.github.franciscosviana.stmservicos.domain.model.*;
import io.github.franciscosviana.stmservicos.domain.model.enums.StatusOrdem;
import io.github.franciscosviana.stmservicos.domain.repository.OrdemServicoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.thymeleaf.TemplateEngine;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("OrdemServicoExportService")
@ExtendWith(MockitoExtension.class)
class OrdemServicoExportServiceTest {

    @Mock
    OrdemServicoRepository repository;

    @Mock
    OrdemServicoOutputAssembler assembler;

    @Mock
    SolucaoService solucaoService;

    @Mock
    OrdemServicoService ordemServicoService;

    @Mock
    TemplateEngine templateEngine;

    @InjectMocks
    OrdemServicoExportService service;

    @Nested
    @DisplayName("exportarXlsx()")
    class ExportarXlsx {

        @Test
        @DisplayName("deve gerar arquivo XLSX não vazio com lista de OS")
        void deveGerarXlsxComDados() {
            OrdemServico os = ordemServicoComDados();
            OrdemServicoOutput output = ordemServicoOutput();

            when(repository.findAll(any(Specification.class))).thenReturn(List.of(os));
            when(assembler.toModel(os)).thenReturn(output);

            byte[] resultado = service.exportarXlsx(
                    null, null, null, null, null, null, null, null, null);

            assertThat(resultado).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("deve gerar XLSX mesmo com lista vazia")
        void deveGerarXlsxVazio() {
            when(repository.findAll(any(Specification.class))).thenReturn(List.of());

            byte[] resultado = service.exportarXlsx(
                    null, null, null, null, null, null, null, null, null);

            assertThat(resultado).isNotNull().isNotEmpty();
        }
    }

    @Nested
    @DisplayName("exportarCsv()")
    class ExportarCsv {

        @Test
        @DisplayName("deve gerar arquivo CSV não vazio com lista de OS")
        void deveGerarCsvComDados() {
            OrdemServico os = ordemServicoComDados();
            OrdemServicoOutput output = ordemServicoOutput();

            when(repository.findAll(any(Specification.class))).thenReturn(List.of(os));
            when(assembler.toModel(os)).thenReturn(output);

            byte[] resultado = service.exportarCsv(
                    null, null, null, null, null, null, null, null, null);

            assertThat(resultado).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("deve gerar CSV com cabeçalho mesmo com lista vazia")
        void deveGerarCsvVazio() {
            when(repository.findAll(any(Specification.class))).thenReturn(List.of());

            byte[] resultado = service.exportarCsv(
                    null, null, null, null, null, null, null, null, null);

            assertThat(resultado).isNotNull().isNotEmpty();
        }
    }

    private OrdemServico ordemServicoComDados() {
        Cliente cliente = new Cliente();
        cliente.setNome("EMPRESA X");

        Contrato contrato = new Contrato();
        contrato.setNomeContrato("Contrato Principal");

        Credenciado credenciado = new Credenciado();
        credenciado.setRag("Credenciado Ltda");

        Tecnico tecnico = new Tecnico();
        tecnico.setNome("João Técnico");

        OrdemServico os = new OrdemServico();
        os.setId(UUID.randomUUID());
        os.setOsg("OSG260001");
        os.setOsClt("OS-CLI-001");
        os.setStatus(StatusOrdem.ABERTA);
        os.setDataHoraAbertura(OffsetDateTime.now());
        os.setCliente(cliente);
        os.setContrato(contrato);
        os.setCredenciado(credenciado);
        os.setTecnico(tecnico);
        os.setEquipamento("Equipamento X");
        os.setDefeito("Defeito Y");
        return os;
    }

    private OrdemServicoOutput ordemServicoOutput() {
        OrdemServicoOutput output = new OrdemServicoOutput();
        output.setId(UUID.randomUUID());
        output.setOsg("OSG260001");
        output.setOsClt("OS-CLI-001");
        return output;
    }
}
