package io.github.franciscosviana.stmservicos.api.controller;

import io.github.franciscosviana.stmservicos.common.client.model.ViaCepResponse;
import io.github.franciscosviana.stmservicos.config.TestSecurityConfig;
import io.github.franciscosviana.stmservicos.domain.service.EnderecoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser
@Import(TestSecurityConfig.class)
@WebMvcTest(EnderecoController.class)
@DisplayName("EnderecoController (MockMvc)")
class EnderecoControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    EnderecoService enderecoService;

    @Test
    @DisplayName("GET /api/enderecos/cep/{cep} deve retornar dados do endereço")
    void buscarCep() throws Exception {
        ViaCepResponse resp = new ViaCepResponse();
        resp.setCep("64000-000");
        resp.setLogradouro("Av. Frei Serafim");
        resp.setLocalidade("Teresina");
        resp.setUf("PI");
        when(enderecoService.consultarCep("64000-000")).thenReturn(resp);

        mockMvc.perform(get("/api/enderecos/cep/64000-000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cep").value("64000-000"))
                .andExpect(jsonPath("$.localidade").value("Teresina"));
    }
}
