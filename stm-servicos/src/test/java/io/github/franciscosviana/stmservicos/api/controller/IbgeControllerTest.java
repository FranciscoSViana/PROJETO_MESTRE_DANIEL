package io.github.franciscosviana.stmservicos.api.controller;

import io.github.franciscosviana.stmservicos.api.model.output.EstadoOutput;
import io.github.franciscosviana.stmservicos.api.model.output.MunicipioOutput;
import io.github.franciscosviana.stmservicos.config.TestSecurityConfig;
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

@WithMockUser
@Import(TestSecurityConfig.class)
@WebMvcTest(IbgeController.class)
@DisplayName("IbgeController (MockMvc)")
class IbgeControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean IbgeService ibgeService;

    @Test
    @DisplayName("GET /api/ibge/estados deve retornar lista de estados")
    void listarEstados() throws Exception {
        EstadoOutput estado = new EstadoOutput("Piauí", "PI");
        when(ibgeService.listarEstados()).thenReturn(List.of(estado));

        mockMvc.perform(get("/api/ibge/estados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Piauí"))
                .andExpect(jsonPath("$[0].sigla").value("PI"));
    }

    @Test
    @DisplayName("GET /api/ibge/estados/{uf}/municipios deve retornar lista de municípios")
    void listarMunicipios() throws Exception {
        MunicipioOutput municipio = new MunicipioOutput("Teresina", "2211001");
        when(ibgeService.listarMunicipios("PI")).thenReturn(List.of(municipio));

        mockMvc.perform(get("/api/ibge/estados/PI/municipios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Teresina"));
    }
}
