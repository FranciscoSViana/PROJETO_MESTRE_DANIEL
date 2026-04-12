package io.github.franciscosviana.stmservicos.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.franciscosviana.stmservicos.api.model.output.TecnicoOutput;
import io.github.franciscosviana.stmservicos.config.TestSecurityConfig;
import io.github.franciscosviana.stmservicos.domain.service.TecnicoService;
import org.junit.jupiter.api.DisplayName;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser
@Import(TestSecurityConfig.class)
@WebMvcTest(CredenciadoTecnicoController.class)
@DisplayName("CredenciadoTecnicoController (MockMvc)")
class CredenciadoTecnicoControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean TecnicoService tecnicoService;

    @Test
    @DisplayName("POST /{credenciadoId}/tecnicos deve retornar 200 ao adicionar técnico")
    void adicionarTecnico() throws Exception {
        UUID credId = UUID.randomUUID();
        TecnicoOutput output = new TecnicoOutput();
        when(tecnicoService.salvar(eq(credId), any())).thenReturn(output);

        mockMvc.perform(post("/api/credenciados/" + credId + "/tecnicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Técnico A\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /{credenciadoId}/tecnicos deve retornar página de técnicos")
    void listarTecnicos() throws Exception {
        UUID credId = UUID.randomUUID();
        when(tecnicoService.listarPorCredenciado(eq(credId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/credenciados/" + credId + "/tecnicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /tecnicos/{tecnicoId} deve retornar técnico")
    void buscarTecnico() throws Exception {
        UUID tecId = UUID.randomUUID();
        when(tecnicoService.buscarPorId(tecId)).thenReturn(new TecnicoOutput());

        mockMvc.perform(get("/api/credenciados/tecnicos/" + tecId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /tecnicos/{tecnicoId} deve retornar técnico atualizado")
    void atualizarTecnico() throws Exception {
        UUID tecId = UUID.randomUUID();
        when(tecnicoService.atualizar(eq(tecId), any())).thenReturn(new TecnicoOutput());

        mockMvc.perform(put("/api/credenciados/tecnicos/" + tecId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Técnico B\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /tecnicos/{tecnicoId} deve retornar 204")
    void excluirTecnico() throws Exception {
        UUID tecId = UUID.randomUUID();
        doNothing().when(tecnicoService).excluir(tecId);

        mockMvc.perform(delete("/api/credenciados/tecnicos/" + tecId))
                .andExpect(status().isNoContent());
    }
}
