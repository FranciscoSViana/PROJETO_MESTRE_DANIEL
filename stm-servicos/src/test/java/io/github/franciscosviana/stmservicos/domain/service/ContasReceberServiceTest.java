package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.model.input.ContasReceberFilter;
import io.github.franciscosviana.stmservicos.api.model.output.ContasReceberOutput;
import io.github.franciscosviana.stmservicos.api.model.output.ContasReceberTotaisOutput;
import io.github.franciscosviana.stmservicos.domain.repository.ContasReceberRepository;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("ContasReceberService")
@ExtendWith(MockitoExtension.class)
class ContasReceberServiceTest {

    @Mock
    ContasReceberRepository contasReceberRepository;

    @Mock
    TemplateEngine templateEngine;

    @InjectMocks
    ContasReceberService service;

    @Nested
    @DisplayName("listar()")
    class Listar {

        @Test
        @DisplayName("deve retornar página de ContasReceberOutput filtrada")
        void deveListarComFiltro() {
            ContasReceberFilter filtro = new ContasReceberFilter();
            PageRequest pageable = PageRequest.of(0, 10);

            ContasReceberOutput output = new ContasReceberOutput();
            Page<ContasReceberOutput> page = new PageImpl<>(List.of(output));

            when(contasReceberRepository.buscarComFiltro(filtro, pageable)).thenReturn(page);

            Page<ContasReceberOutput> result = service.listar(filtro, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(contasReceberRepository).buscarComFiltro(filtro, pageable);
        }

        @Test
        @DisplayName("deve retornar página vazia quando não há resultados")
        void deveRetornarPaginaVazia() {
            ContasReceberFilter filtro = new ContasReceberFilter();
            PageRequest pageable = PageRequest.of(0, 10);

            when(contasReceberRepository.buscarComFiltro(filtro, pageable))
                    .thenReturn(Page.empty());

            Page<ContasReceberOutput> result = service.listar(filtro, pageable);

            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("buscarTotais()")
    class BuscarTotais {

        @Test
        @DisplayName("deve retornar totais calculados do repositório")
        void deveBuscarTotais() {
            ContasReceberFilter filtro = new ContasReceberFilter();
            ContasReceberTotaisOutput totais = ContasReceberTotaisOutput.builder()
                    .qtdTotal(5L).qtdRecebido(3L).qtdNaoRecebido(2L)
                    .totalGeral(new BigDecimal("875.00"))
                    .build();

            when(contasReceberRepository.buscarTotais(filtro)).thenReturn(totais);

            ContasReceberTotaisOutput result = service.buscarTotais(filtro);

            assertThat(result).isEqualTo(totais);
            verify(contasReceberRepository).buscarTotais(filtro);
        }
    }

    @Nested
    @DisplayName("listarLotes()")
    class ListarLotes {

        @Test
        @DisplayName("deve retornar lista de lotes distintos")
        void deveListarLotes() {
            when(contasReceberRepository.buscarLotesDistintos())
                    .thenReturn(List.of("LOTE-01", "LOTE-02"));

            List<String> result = service.listarLotes();

            assertThat(result).containsExactly("LOTE-01", "LOTE-02");
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há lotes")
        void deveRetornarListaVazia() {
            when(contasReceberRepository.buscarLotesDistintos()).thenReturn(List.of());

            List<String> result = service.listarLotes();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("listarLotesPorCliente()")
    class ListarLotesPorCliente {

        @Test
        @DisplayName("deve retornar lotes do cliente informado")
        void deveListarLotesPorCliente() {
            when(contasReceberRepository.buscarLotesPorCliente("EMPRESA X"))
                    .thenReturn(List.of("LOTE-01"));

            List<String> result = service.listarLotesPorCliente("EMPRESA X");

            assertThat(result).containsExactly("LOTE-01");
            verify(contasReceberRepository).buscarLotesPorCliente("EMPRESA X");
        }
    }

    @Nested
    @DisplayName("listarOsPendentes()")
    class ListarOsPendentes {

        @Test
        @DisplayName("deve retornar OS pendentes para o cliente e lote informados")
        void deveListarOsPendentes() {
            ContasReceberOutput os = new ContasReceberOutput();
            when(contasReceberRepository.buscarTodosComFiltro(any(ContasReceberFilter.class)))
                    .thenReturn(List.of(os));

            List<ContasReceberOutput> result = service.listarOsPendentes("EMPRESA X", "LOTE-01");

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("exportarXlsx()")
    class ExportarXlsx {

        @Test
        @DisplayName("deve retornar array de bytes não vazio ao exportar")
        void deveExportarXlsx() {
            ContasReceberOutput dado = contasReceberOutputComValores();
            when(contasReceberRepository.buscarTodosComFiltro(any(ContasReceberFilter.class)))
                    .thenReturn(List.of(dado));

            byte[] resultado = service.exportarXlsx(new ContasReceberFilter());

            assertThat(resultado).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("deve gerar XLSX mesmo com lista vazia")
        void deveGerarXlsxComListaVazia() {
            when(contasReceberRepository.buscarTodosComFiltro(any()))
                    .thenReturn(List.of());

            byte[] resultado = service.exportarXlsx(new ContasReceberFilter());

            assertThat(resultado).isNotNull().isNotEmpty();
        }
    }

    private ContasReceberOutput contasReceberOutputComValores() {
        ContasReceberOutput o = new ContasReceberOutput();
        o.setOsg("OSG260001");
        o.setOsClt("OS-CLI-001");
        o.setCliente("EMPRESA X");
        o.setValorChamado(new BigDecimal("150.00"));
        o.setKm(new BigDecimal("10"));
        o.setValorKm(new BigDecimal("2.50"));
        o.setPedagio(BigDecimal.ZERO);
        o.setEstacionamento(BigDecimal.ZERO);
        o.setValorOutros(BigDecimal.ZERO);
        o.setValorTotal(new BigDecimal("175.00"));
        return o;
    }
}
