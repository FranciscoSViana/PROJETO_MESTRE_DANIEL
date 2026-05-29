package io.github.franciscosviana.stmservicos.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.franciscosviana.stmservicos.api.model.input.OrdemServicoInput;
import io.github.franciscosviana.stmservicos.api.model.output.OrdemServicoOutput;
import io.github.franciscosviana.stmservicos.common.validation.OrdemServicoException;
import io.github.franciscosviana.stmservicos.config.TestSecurityConfig;
import io.github.franciscosviana.stmservicos.domain.service.HistoricoOrdemServicoService;
import io.github.franciscosviana.stmservicos.domain.service.OrdemServicoExportService;
import io.github.franciscosviana.stmservicos.domain.service.OrdemServicoService;
import io.github.franciscosviana.stmservicos.domain.service.SolucaoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser
@Import(TestSecurityConfig.class)
@WebMvcTest(OrdemServicoController.class)
@DisplayName("OrdemServicoController (MockMvc)")
class OrdemServicoControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    OrdemServicoService ordemServicoService;
    @MockitoBean
    OrdemServicoExportService exportService;
    @MockitoBean
    SolucaoService solucaoService;
    @MockitoBean
    HistoricoOrdemServicoService historicoOrdemServicoService;

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/ordens-servico
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/ordens-servico")
    class Criar {

        @Test
        @DisplayName("deve retornar 200 ao criar OS com sucesso")
        void deveRetornar200AoCriar() throws Exception {
            OrdemServicoInput input = new OrdemServicoInput();
            input.setClienteId(UUID.randomUUID());
            input.setContratoId(UUID.randomUUID());
            input.setTecnicoId(UUID.randomUUID());
            input.setCredenciadoId(UUID.randomUUID());

            OrdemServicoOutput output = new OrdemServicoOutput();
            when(ordemServicoService.salvar(any())).thenReturn(output);

            mockMvc.perform(post("/api/ordens-servico")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(input)))
                    .andExpect(status().isOk());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/ordens-servico
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/ordens-servico")
    class Listar {

        @Test
        @DisplayName("deve retornar 200 com página vazia quando não há OS")
        void deveRetornarPaginaVazia() throws Exception {
            when(ordemServicoService.listar(any(Pageable.class), any(), any(), any(),
                    any(), any(), any(), any(), any(), any()))
                    .thenReturn(new PageImpl<>(Collections.emptyList()));

            mockMvc.perform(get("/api/ordens-servico"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }

        @Test
        @DisplayName("deve aceitar parâmetros de filtro sem erro")
        void deveAceitarFiltros() throws Exception {
            when(ordemServicoService.listar(any(), any(), any(), any(),
                    any(), any(), any(), any(), any(), any()))
                    .thenReturn(new PageImpl<>(Collections.emptyList()));

            mockMvc.perform(get("/api/ordens-servico")
                            .param("status", "ABERTA")
                            .param("cliente", "Empresa X"))
                    .andExpect(status().isOk());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/ordens-servico/{id}
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/ordens-servico/{id}")
    class BuscarPorId {

        @Test
        @DisplayName("deve retornar 200 com OS quando encontrada")
        void deveRetornar200QuandoEncontrada() throws Exception {
            UUID id = UUID.randomUUID();
            OrdemServicoOutput output = new OrdemServicoOutput();
            when(ordemServicoService.buscarPorId(id)).thenReturn(output);

            mockMvc.perform(get("/api/ordens-servico/" + id))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("deve propagar exceção quando OS não encontrada")
        void devePropagarExcecaoQuandoNaoEncontrada() throws Exception {
            UUID id = UUID.randomUUID();
            when(ordemServicoService.buscarPorId(id))
                    .thenThrow(new OrdemServicoException("Ordem de Serviço não encontrada"));

            // A exceção vai ser tratada pelo ApiExceptionHandler
            mockMvc.perform(get("/api/ordens-servico/" + id))
                    .andExpect(status().is5xxServerError()); // sem handler custom, retorna 500
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // DELETE /api/ordens-servico/{id}
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("DELETE /api/ordens-servico/{id} deve retornar 200 ao deletar")
    void deveDeletar() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(ordemServicoService).deletar(id);

        mockMvc.perform(delete("/api/ordens-servico/" + id))
                .andExpect(status().isOk());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/ordens-servico/proximo-osg
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("GET /api/ordens-servico/proximo-osg deve retornar próximo OSG")
    void deveRetornarProximoOsg() throws Exception {
        when(ordemServicoService.gerarProximoOsg()).thenReturn("OSG260001");

        mockMvc.perform(get("/api/ordens-servico/proximo-osg"))
                .andExpect(status().isOk())
                .andExpect(content().string("OSG260001"));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // PATCH /api/ordens-servico/{id}/rastreio
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("PATCH /api/ordens-servico/{id}/rastreio deve atualizar status rastreio")
    void deveAtualizarStatusRastreio() throws Exception {
        UUID id = UUID.randomUUID();
        OrdemServicoOutput output = new OrdemServicoOutput();
        when(ordemServicoService.atualizarStatusRastreio(eq(id), eq("CHEGOU"))).thenReturn(output);

        mockMvc.perform(patch("/api/ordens-servico/" + id + "/rastreio")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statusRastreio\":\"CHEGOU\"}"))
                .andExpect(status().isOk());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/ordens-servico/{id}/historico
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("GET /api/ordens-servico/{id}/historico deve retornar lista de histórico")
    void deveRetornarHistorico() throws Exception {
        UUID id = UUID.randomUUID();
        when(historicoOrdemServicoService.listarPorOrdemServico(id)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/ordens-servico/" + id + "/historico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // PUT /api/ordens-servico/{id}
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("PUT /api/ordens-servico/{id} deve retornar 200 ao atualizar")
    void deveAtualizar() throws Exception {
        UUID id = UUID.randomUUID();
        OrdemServicoInput input = new OrdemServicoInput();
        OrdemServicoOutput output = new OrdemServicoOutput();
        when(ordemServicoService.atualizar(eq(id), any())).thenReturn(output);

        mockMvc.perform(put("/api/ordens-servico/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // POST /api/ordens-servico/{ordemId}/solucao
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("POST /api/ordens-servico/{ordemId}/solucao deve finalizar OS")
    void deveFinalizar() throws Exception {
        UUID id = UUID.randomUUID();
        io.github.franciscosviana.stmservicos.api.model.output.SolucaoOSOutput output =
                new io.github.franciscosviana.stmservicos.api.model.output.SolucaoOSOutput();
        when(solucaoService.finalizarOS(eq(id), any())).thenReturn(output);

        mockMvc.perform(post("/api/ordens-servico/" + id + "/solucao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // PUT /api/ordens-servico/{ordemId}/solucao
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("PUT /api/ordens-servico/{ordemId}/solucao deve editar solução")
    void deveEditarSolucao() throws Exception {
        UUID id = UUID.randomUUID();
        io.github.franciscosviana.stmservicos.api.model.output.SolucaoOSOutput output =
                new io.github.franciscosviana.stmservicos.api.model.output.SolucaoOSOutput();
        when(solucaoService.editarSolucao(eq(id), any())).thenReturn(output);

        mockMvc.perform(put("/api/ordens-servico/" + id + "/solucao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/ordens-servico/exportar/xlsx
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("GET /api/ordens-servico/exportar/xlsx deve retornar bytes do arquivo")
    void deveExportarXlsx() throws Exception {
        when(exportService.exportarXlsx(any(), any(), any(), any(), any(),
                any(), any(), any(), any()))
                .thenReturn(new byte[]{0x50, 0x4B});

        mockMvc.perform(get("/api/ordens-servico/exportar/xlsx"))
                .andExpect(status().isOk());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/ordens-servico/exportar/csv
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("GET /api/ordens-servico/exportar/csv deve retornar bytes do arquivo")
    void deveExportarCsv() throws Exception {
        when(exportService.exportarCsv(any(), any(), any(), any(), any(),
                any(), any(), any(), any()))
                .thenReturn("osg,osClt".getBytes());

        mockMvc.perform(get("/api/ordens-servico/exportar/csv"))
                .andExpect(status().isOk());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/ordens-servico/exportar/pdf
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("GET /api/ordens-servico/exportar/pdf deve retornar bytes do arquivo")
    void deveExportarPdf() throws Exception {
        when(exportService.exportarPdf(any(), any(), any(), any(), any(),
                any(), any(), any(), any()))
                .thenReturn(new byte[]{0x25, 0x50});

        mockMvc.perform(get("/api/ordens-servico/exportar/pdf"))
                .andExpect(status().isOk());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/ordens-servico/{id}/relatorio/pdf
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("GET /api/ordens-servico/{id}/relatorio/pdf deve retornar relatório PDF")
    void deveRetornarRelatorioPdf() throws Exception {
        UUID id = UUID.randomUUID();
        when(exportService.exportarRelatorioPdf(id)).thenReturn(new byte[]{0x25, 0x50});

        mockMvc.perform(get("/api/ordens-servico/" + id + "/relatorio/pdf"))
                .andExpect(status().isOk());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/ordens-servico/{id}/relatorio/xlsx
    // ──────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("GET /api/ordens-servico/{id}/relatorio/xlsx deve retornar relatório XLSX")
    void deveRetornarRelatorioXlsx() throws Exception {
        UUID id = UUID.randomUUID();
        when(exportService.exportarRelatorioXlsx(id)).thenReturn(new byte[]{0x50, 0x4B});

        mockMvc.perform(get("/api/ordens-servico/" + id + "/relatorio/xlsx"))
                .andExpect(status().isOk());
    }
}
