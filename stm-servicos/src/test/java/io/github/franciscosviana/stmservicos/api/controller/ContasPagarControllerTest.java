package io.github.franciscosviana.stmservicos.api.controller;

import io.github.franciscosviana.stmservicos.api.model.output.ContasPagarTotaisOutput;
import io.github.franciscosviana.stmservicos.config.TestSecurityConfig;
import io.github.franciscosviana.stmservicos.domain.service.ContasPagarService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser
@Import(TestSecurityConfig.class)
@WebMvcTest(ContasPagarController.class)
@DisplayName("ContasPagarController (MockMvc)")
class ContasPagarControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean ContasPagarService contasPagarService;

    @Test
    @DisplayName("GET /api/financeiro/contas-pagar deve retornar página")
    void listar() throws Exception {
        when(contasPagarService.listar(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/financeiro/contas-pagar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/financeiro/contas-pagar/totais deve retornar totais")
    void totais() throws Exception {
        when(contasPagarService.buscarTotais(any())).thenReturn(new ContasPagarTotaisOutput());

        mockMvc.perform(get("/api/financeiro/contas-pagar/totais"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/financeiro/contas-pagar/lotes deve retornar lista de lotes")
    void lotes() throws Exception {
        when(contasPagarService.listarLotes()).thenReturn(List.of("LOTE-01", "LOTE-02"));

        mockMvc.perform(get("/api/financeiro/contas-pagar/lotes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("LOTE-01"));
    }

    @Test
    @DisplayName("GET /api/financeiro/contas-pagar/exportar/xlsx deve retornar bytes")
    void exportarXlsx() throws Exception {
        when(contasPagarService.exportarXlsx(any())).thenReturn("bytes".getBytes());

        mockMvc.perform(get("/api/financeiro/contas-pagar/exportar/xlsx"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("contas-pagar.xlsx")));
    }

    @Test
    @DisplayName("GET /api/financeiro/contas-pagar/exportar/pdf deve retornar bytes")
    void exportarPdf() throws Exception {
        when(contasPagarService.exportarPdf(any())).thenReturn(new byte[]{0x25, 0x50});

        mockMvc.perform(get("/api/financeiro/contas-pagar/exportar/pdf"))
                .andExpect(status().isOk());
    }
}
