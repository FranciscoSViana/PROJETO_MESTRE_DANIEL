package io.github.franciscosviana.stmservicos.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.franciscosviana.stmservicos.api.model.input.ClienteInput;
import io.github.franciscosviana.stmservicos.api.model.input.ContratoInput;
import io.github.franciscosviana.stmservicos.api.model.input.PagamentoOSInput;
import io.github.franciscosviana.stmservicos.api.model.input.TecnicoInput;
import io.github.franciscosviana.stmservicos.api.model.output.*;
import io.github.franciscosviana.stmservicos.common.client.model.EstadoResponse;
import io.github.franciscosviana.stmservicos.common.client.model.MunicipioResponse;
import io.github.franciscosviana.stmservicos.common.client.model.ReceitaWsResponse;
import io.github.franciscosviana.stmservicos.common.client.model.ViaCepResponse;
import io.github.franciscosviana.stmservicos.common.validation.ClienteException;
import io.github.franciscosviana.stmservicos.config.TestSecurityConfig;
import io.github.franciscosviana.stmservicos.domain.model.enums.StatusRastreio;
import io.github.franciscosviana.stmservicos.domain.service.*;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// ══════════════════════════════════════════════════════════════════════════════
// ClienteController
// ══════════════════════════════════════════════════════════════════════════════
@WithMockUser
@Import(TestSecurityConfig.class)
@WebMvcTest(ClienteController.class)
@DisplayName("ClienteController (MockMvc)")
class ClienteControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean ClienteService clienteService;

    @Test
    @DisplayName("GET /api/clientes/{id} deve retornar 200 quando encontrado")
    void buscarPorId() throws Exception {
        UUID id = UUID.randomUUID();
        when(clienteService.buscarPorId(id)).thenReturn(mock(ClienteOutput.class));

        mockMvc.perform(get("/api/clientes/" + id))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/clientes deve retornar página")
    void listar() throws Exception {
        when(clienteService.listar(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @DisplayName("POST /api/clientes deve retornar 200 ao salvar")
    void salvar() throws Exception {
        ClienteInput input = new ClienteInput();
        input.setCnpj("12345678000195");
        when(clienteService.salvar(any())).thenReturn(mock(ClienteOutput.class));

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/clientes/{id} deve retornar 200 ao atualizar")
    void atualizar() throws Exception {
        UUID id = UUID.randomUUID();
        when(clienteService.atualizar(eq(id), any())).thenReturn(mock(ClienteOutput.class));

        mockMvc.perform(put("/api/clientes/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/clientes/{id} deve retornar 204")
    void excluir() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(clienteService).excluir(id);

        mockMvc.perform(delete("/api/clientes/" + id))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/clientes/codigo/{codigo} deve retornar 200")
    void buscarPorCodigo() throws Exception {
        when(clienteService.buscarPorCodigo(1L)).thenReturn(mock(ClienteOutput.class));

        mockMvc.perform(get("/api/clientes/codigo/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/clientes/cnpj/{cnpj} deve retornar dados da Receita")
    void consultarCnpj() throws Exception {
        ReceitaWsResponse resp = new ReceitaWsResponse();
        resp.setRazaoSocial("EMPRESA X");
        when(clienteService.consultarCnpj("12345678000195")).thenReturn(resp);

        mockMvc.perform(get("/api/clientes/cnpj/12345678000195"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.razao_social").value("EMPRESA X"));
    }
}
