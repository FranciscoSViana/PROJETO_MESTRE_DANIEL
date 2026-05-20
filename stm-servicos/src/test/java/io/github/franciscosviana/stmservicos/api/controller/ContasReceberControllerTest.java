package io.github.franciscosviana.stmservicos.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.franciscosviana.stmservicos.api.exceptionhandler.ApiExceptionHandler;
import io.github.franciscosviana.stmservicos.api.model.output.ContasReceberOutput;
import io.github.franciscosviana.stmservicos.api.model.output.ContasReceberTotaisOutput;
import io.github.franciscosviana.stmservicos.config.TestSecurityConfig;
import io.github.franciscosviana.stmservicos.domain.service.ContasReceberService;
import io.github.franciscosviana.stmservicos.domain.service.PagamentoClienteOSService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContasReceberController.class)
@DisplayName("ContasReceberController (MockMvc)")
@Import({TestSecurityConfig.class, ApiExceptionHandler.class})
class ContasReceberControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    ContasReceberService contasReceberService;

    @MockitoBean
    PagamentoClienteOSService pagamentoClienteOSService;

    @Nested
    @DisplayName("GET /api/financeiro/contas-receber")
    class Listar {

        @Test
        @DisplayName("deve retornar 200 com página de contas a receber")
        void deveRetornar200ComPagina() throws Exception {
            ContasReceberOutput output = new ContasReceberOutput();
            output.setOsg("OSG260001");
            output.setCliente("EMPRESA X");
            output.setValorTotal(new BigDecimal("175.00"));

            Page<ContasReceberOutput> page = new PageImpl<>(List.of(output));
            when(contasReceberService.listar(any(), any())).thenReturn(page);

            mockMvc.perform(get("/api/financeiro/contas-receber"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].osg").value("OSG260001"));
        }

        @Test
        @DisplayName("deve retornar 200 com página vazia")
        void deveRetornar200PaginaVazia() throws Exception {
            when(contasReceberService.listar(any(), any())).thenReturn(Page.empty());

            mockMvc.perform(get("/api/financeiro/contas-receber"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/financeiro/contas-receber/totais")
    class BuscarTotais {

        @Test
        @DisplayName("deve retornar 200 com totais calculados")
        void deveRetornar200ComTotais() throws Exception {
            ContasReceberTotaisOutput totais = ContasReceberTotaisOutput.builder()
                    .qtdTotal(10L)
                    .totalGeral(new BigDecimal("1000.00"))
                    .build();

            when(contasReceberService.buscarTotais(any())).thenReturn(totais);

            mockMvc.perform(get("/api/financeiro/contas-receber/totais"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalGeral").value(1000.00));
        }
    }

    @Nested
    @DisplayName("GET /api/financeiro/contas-receber/lotes")
    class ListarLotes {

        @Test
        @DisplayName("deve retornar 200 com lista de lotes")
        void deveRetornar200ComLotes() throws Exception {
            when(contasReceberService.listarLotes()).thenReturn(List.of("LOTE-01", "LOTE-02"));

            mockMvc.perform(get("/api/financeiro/contas-receber/lotes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0]").value("LOTE-01"));
        }
    }

    @Nested
    @DisplayName("GET /api/financeiro/contas-receber/exportar/xlsx")
    class ExportarXlsx {

        @Test
        @DisplayName("deve retornar 200 com Content-Disposition de planilha")
        void deveRetornar200ComXlsx() throws Exception {
            byte[] xlsxBytes = new byte[]{80, 75, 3, 4};
            when(contasReceberService.exportarXlsx(any())).thenReturn(xlsxBytes);

            mockMvc.perform(get("/api/financeiro/contas-receber/exportar/xlsx"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition",
                            org.hamcrest.Matchers.containsString("contas-receber")));
        }
    }
}
