package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.model.input.ContasPagarFilter;
import io.github.franciscosviana.stmservicos.api.model.output.ContasPagarOutput;
import io.github.franciscosviana.stmservicos.api.model.output.ContasPagarTotaisOutput;
import io.github.franciscosviana.stmservicos.domain.model.enums.StatusOrdem;
import io.github.franciscosviana.stmservicos.domain.model.enums.TipoPagamento;
import io.github.franciscosviana.stmservicos.domain.repository.ContasPagarRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.thymeleaf.TemplateEngine;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContasPagarService")
class ContasPagarServiceTest {

    @Mock ContasPagarRepository contasPagarRepository;
    @Mock TemplateEngine templateEngine;
    @InjectMocks ContasPagarService service;

    // ──────────────────────────────────────────────────────────────────────────
    // listar()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("listar()")
    class Listar {

        @Test
        @DisplayName("deve delegar para repository.buscarComFiltro e retornar página")
        void deveDelegarParaRepository() {
            ContasPagarFilter filtro = new ContasPagarFilter();
            PageRequest pageable = PageRequest.of(0, 10);
            Page<ContasPagarOutput> page = new PageImpl<>(Collections.emptyList());

            when(contasPagarRepository.buscarComFiltro(filtro, pageable)).thenReturn(page);

            Page<ContasPagarOutput> result = service.listar(filtro, pageable);

            assertThat(result).isEqualTo(page);
            verify(contasPagarRepository).buscarComFiltro(filtro, pageable);
        }

        @Test
        @DisplayName("listarTodos() deve retornar lista completa sem paginação")
        void deveListarTodos() {
            ContasPagarOutput item = new ContasPagarOutput();
            when(contasPagarRepository.buscarTodosComFiltro(any()))
                    .thenReturn(List.of(item));

            List<ContasPagarOutput> result = service.listarTodos(new ContasPagarFilter());

            assertThat(result).hasSize(1);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // buscarTotais()
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("buscarTotais() deve delegar para repository")
    void deveBuscarTotais() {
        ContasPagarTotaisOutput totais = new ContasPagarTotaisOutput();
        when(contasPagarRepository.buscarTotais(any())).thenReturn(totais);

        ContasPagarTotaisOutput result = service.buscarTotais(new ContasPagarFilter());

        assertThat(result).isEqualTo(totais);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // listarLotes()
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("listarLotes() deve retornar lista de lotes distintos")
    void deveListarLotes() {
        when(contasPagarRepository.buscarLotesDistintos())
                .thenReturn(List.of("LOTE-01", "LOTE-02", "LOTE-03"));

        List<String> result = service.listarLotes();

        assertThat(result).containsExactly("LOTE-01", "LOTE-02", "LOTE-03");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // exportarXlsx()
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("exportarXlsx()")
    class ExportarXlsx {

        @Test
        @DisplayName("deve retornar array de bytes não vazio com dados vazios")
        void deveRetornarBytesVazios() {
            when(contasPagarRepository.buscarTodosComFiltro(any()))
                    .thenReturn(Collections.emptyList());

            byte[] result = service.exportarXlsx(new ContasPagarFilter());

            assertThat(result).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("deve gerar XLSX com cabeçalhos quando há dados")
        void deveGerarXlsxComDados() {
            ContasPagarOutput item = criarContasPagarOutput();
            when(contasPagarRepository.buscarTodosComFiltro(any()))
                    .thenReturn(List.of(item));

            byte[] result = service.exportarXlsx(new ContasPagarFilter());

            // XLSX válido começa com magic bytes PK (zip format)
            assertThat(result).isNotEmpty();
            assertThat(result[0]).isEqualTo((byte) 0x50); // 'P'
            assertThat(result[1]).isEqualTo((byte) 0x4B); // 'K'
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────
    private ContasPagarOutput criarContasPagarOutput() {
        ContasPagarOutput item = new ContasPagarOutput();
        item.setOrdemServicoId(UUID.randomUUID());
        item.setOsg("OSG260001");
        item.setOsClt("CLT-001");
        item.setCliente("Empresa X");
        item.setCredenciadoRag("Credenciado Y");
        item.setStatusOrdem(StatusOrdem.CONCLUIDA);
        item.setDataHoraAbertura(OffsetDateTime.now());
        item.setValorChamado(new BigDecimal("100.00"));
        item.setKm(new BigDecimal("10"));
        item.setValorKm(new BigDecimal("2.50"));
        item.setPedagio(new BigDecimal("5.00"));
        item.setEstacionamento(new BigDecimal("3.00"));
        item.setValorOutros(new BigDecimal("0.00"));
        item.setValorTotal(new BigDecimal("133.00"));
        item.setPago(true);
        item.setTipoPagamento(TipoPagamento.PIX);
        item.setDataPagamento(OffsetDateTime.now());
        return item;
    }
}
