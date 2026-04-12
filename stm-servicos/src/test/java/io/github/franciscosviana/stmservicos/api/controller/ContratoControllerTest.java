package io.github.franciscosviana.stmservicos.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.franciscosviana.stmservicos.api.model.input.ContratoInput;
import io.github.franciscosviana.stmservicos.api.model.input.PagamentoOSInput;
import io.github.franciscosviana.stmservicos.api.model.output.ContratoOutput;
import io.github.franciscosviana.stmservicos.api.model.output.PagamentoOSOutput;
import io.github.franciscosviana.stmservicos.api.model.output.SolucaoOSOutput;
import io.github.franciscosviana.stmservicos.config.TestSecurityConfig;
import io.github.franciscosviana.stmservicos.domain.model.enums.TipoPagamento;
import io.github.franciscosviana.stmservicos.domain.service.ContratoService;
import io.github.franciscosviana.stmservicos.domain.service.PagamentoOSService;
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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// ══════════════════════════════════════════════════════════════════════════════
// ContratoController
// ══════════════════════════════════════════════════════════════════════════════
@WithMockUser
@Import(TestSecurityConfig.class)
@WebMvcTest(ContratoController.class)
@DisplayName("ContratoController (MockMvc)")
class ContratoControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean ContratoService contratoService;

    @Test
    @DisplayName("POST /api/clientes/{id}/contratos deve retornar 201")
    void adicionar() throws Exception {
        UUID clienteId = UUID.randomUUID();
        ContratoOutput output = new ContratoOutput();
        when(contratoService.adicionar(eq(clienteId), any())).thenReturn(output);

        mockMvc.perform(post("/api/clientes/" + clienteId + "/contratos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("GET /api/clientes/{id}/contratos deve retornar lista")
    void listar() throws Exception {
        UUID clienteId = UUID.randomUUID();
        when(contratoService.listar(clienteId)).thenReturn(List.of());

        mockMvc.perform(get("/api/clientes/" + clienteId + "/contratos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("PUT /api/clientes/{cId}/contratos/{id} deve retornar 200")
    void atualizar() throws Exception {
        UUID cId = UUID.randomUUID();
        UUID ctrId = UUID.randomUUID();
        when(contratoService.atualizar(eq(cId), eq(ctrId), any()))
                .thenReturn(new ContratoOutput());

        mockMvc.perform(put("/api/clientes/" + cId + "/contratos/" + ctrId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/clientes/{cId}/contratos/{id} deve retornar 204")
    void excluir() throws Exception {
        UUID cId = UUID.randomUUID();
        UUID ctrId = UUID.randomUUID();
        doNothing().when(contratoService).excluir(cId, ctrId);

        mockMvc.perform(delete("/api/clientes/" + cId + "/contratos/" + ctrId))
                .andExpect(status().isNoContent());
    }
}
