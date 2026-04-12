package io.github.franciscosviana.stmservicos.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.franciscosviana.stmservicos.api.model.output.SolucaoOSOutput;
import io.github.franciscosviana.stmservicos.config.TestSecurityConfig;
import io.github.franciscosviana.stmservicos.domain.service.SolucaoService;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser
@Import(TestSecurityConfig.class)
@WebMvcTest(SolucaoController.class)
@DisplayName("SolucaoController (MockMvc)")
class SolucaoControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean SolucaoService solucaoService;

    @Test
    @DisplayName("GET /api/solucoes deve retornar página")
    void listarTodas() throws Exception {
        when(solucaoService.listarTodas(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/solucoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/solucoes/{id} deve retornar 200")
    void buscarPorId() throws Exception {
        when(solucaoService.buscarPorId(1L)).thenReturn(new SolucaoOSOutput());

        mockMvc.perform(get("/api/solucoes/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/solucoes/ordem/{ordemId} deve retornar 200")
    void buscarPorOrdem() throws Exception {
        UUID ordemId = UUID.randomUUID();
        when(solucaoService.buscarPorOrdem(ordemId)).thenReturn(new SolucaoOSOutput());

        mockMvc.perform(get("/api/solucoes/ordem/" + ordemId))
                .andExpect(status().isOk());
    }
}
