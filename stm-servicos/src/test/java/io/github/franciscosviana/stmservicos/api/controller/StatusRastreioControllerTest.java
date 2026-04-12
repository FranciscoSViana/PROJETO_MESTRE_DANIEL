package io.github.franciscosviana.stmservicos.api.controller;

import io.github.franciscosviana.stmservicos.api.model.output.EstadoOutput;
import io.github.franciscosviana.stmservicos.api.model.output.MunicipioOutput;
import io.github.franciscosviana.stmservicos.common.client.model.ViaCepResponse;
import io.github.franciscosviana.stmservicos.config.TestSecurityConfig;
import io.github.franciscosviana.stmservicos.domain.service.EnderecoService;
import io.github.franciscosviana.stmservicos.domain.service.IbgeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// ══════════════════════════════════════════════════════════════════════════════
// StatusRastreioController
// ══════════════════════════════════════════════════════════════════════════════
@WithMockUser
@Import(TestSecurityConfig.class)
@WebMvcTest(StatusRastreioController.class)
@DisplayName("StatusRastreioController (MockMvc)")
class StatusRastreioControllerTest {

    @Autowired MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/rastreio/status deve retornar todos os status com value, descricao e cor")
    void listar() throws Exception {
        mockMvc.perform(get("/api/rastreio/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].value").exists())
                .andExpect(jsonPath("$[0].descricao").exists())
                .andExpect(jsonPath("$[0].cor").exists());
    }

    @Test
    @DisplayName("GET /api/rastreio/status deve retornar 5 itens (um por enum)")
    void listarQuantidade() throws Exception {
        mockMvc.perform(get("/api/rastreio/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
    }
}
