package io.github.franciscosviana.stmservicos.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.franciscosviana.stmservicos.api.model.output.CredenciadoOutput;
import io.github.franciscosviana.stmservicos.api.model.output.ContasPagarTotaisOutput;
import io.github.franciscosviana.stmservicos.config.TestSecurityConfig;
import io.github.franciscosviana.stmservicos.domain.service.ContasPagarService;
import io.github.franciscosviana.stmservicos.domain.service.CredenciadoService;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser
@Import(TestSecurityConfig.class)
@WebMvcTest(CredenciadoController.class)
@DisplayName("CredenciadoController (MockMvc)")
class CredenciadoControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean CredenciadoService credenciadoService;

    @Test
    @DisplayName("GET /api/credenciados deve retornar página de credenciados")
    void listar() throws Exception {
        when(credenciadoService.listarComFiltro(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/credenciados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("GET /api/credenciados/{id} deve retornar credenciado")
    void buscarPorId() throws Exception {
        UUID id = UUID.randomUUID();
        when(credenciadoService.buscarPorId(id)).thenReturn(mock(CredenciadoOutput.class));

        mockMvc.perform(get("/api/credenciados/" + id))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/credenciados/{id} deve retornar 204")
    void excluir() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(credenciadoService).excluir(id);

        mockMvc.perform(delete("/api/credenciados/" + id))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/credenciados/estados deve retornar lista de estados")
    void listarEstados() throws Exception {
        when(credenciadoService.listarEstados()).thenReturn(List.of());

        mockMvc.perform(get("/api/credenciados/estados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("POST /api/credenciados deve criar credenciado e retornar 201")
    void salvar() throws Exception {
        CredenciadoOutput output = new CredenciadoOutput();
        when(credenciadoService.salvar(any())).thenReturn(output);

        mockMvc.perform(post("/api/credenciados")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"rag\":\"Empresa Ltda\",\"tipoPessoa\":2}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("PUT /api/credenciados/{id} deve atualizar credenciado e retornar 200")
    void atualizar() throws Exception {
        UUID id = UUID.randomUUID();
        CredenciadoOutput output = new CredenciadoOutput();
        when(credenciadoService.atualizar(eq(id), any())).thenReturn(output);

        mockMvc.perform(put("/api/credenciados/" + id)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"rag\":\"Empresa Ltda Atualizada\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/credenciados/credenciado/{codigo} deve retornar credenciado por código")
    void buscarPorCodigo() throws Exception {
        CredenciadoOutput output = new CredenciadoOutput();
        when(credenciadoService.buscarPorCodigo(1L)).thenReturn(output);

        mockMvc.perform(get("/api/credenciados/credenciado/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/credenciados/proximos deve retornar credenciados próximos")
    void buscarProximos() throws Exception {
        when(credenciadoService.buscarProximosPorCep(eq("64000-000"), anyDouble()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/credenciados/proximos")
                        .param("cep", "64000-000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/credenciados/municipios/{uf} deve retornar municípios")
    void listarMunicipios() throws Exception {
        when(credenciadoService.listarMunicipios("PI")).thenReturn(List.of());

        mockMvc.perform(get("/api/credenciados/municipios/PI"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
