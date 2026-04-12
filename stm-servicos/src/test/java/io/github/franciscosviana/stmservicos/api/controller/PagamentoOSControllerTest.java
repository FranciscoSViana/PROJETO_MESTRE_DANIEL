package io.github.franciscosviana.stmservicos.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.franciscosviana.stmservicos.api.model.input.PagamentoOSInput;
import io.github.franciscosviana.stmservicos.api.model.output.PagamentoOSOutput;
import io.github.franciscosviana.stmservicos.api.model.output.SolucaoOSOutput;
import io.github.franciscosviana.stmservicos.config.TestSecurityConfig;
import io.github.franciscosviana.stmservicos.domain.model.enums.TipoPagamento;
import io.github.franciscosviana.stmservicos.domain.service.PagamentoOSService;
import io.github.franciscosviana.stmservicos.domain.service.SolucaoService;
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

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser
@Import(TestSecurityConfig.class)
@WebMvcTest(PagamentoOSController.class)
@DisplayName("PagamentoOSController (MockMvc)")
class PagamentoOSControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean PagamentoOSService pagamentoOSService;

    private PagamentoOSInput inputValido() {
        PagamentoOSInput input = new PagamentoOSInput();
        input.setValorChamado(new BigDecimal("100.00"));
        input.setValorKm(new BigDecimal("2.50"));
        input.setKm(BigDecimal.ZERO);
        input.setPedagio(BigDecimal.ZERO);
        input.setEstacionamento(BigDecimal.ZERO);
        input.setValorOutros(BigDecimal.ZERO);
        input.setTipoPagamento(TipoPagamento.PIX);
        return input;
    }

    @Test
    @DisplayName("POST deve retornar 200 ao registrar pagamento")
    void registrar() throws Exception {
        UUID osId = UUID.randomUUID();
        PagamentoOSOutput output = new PagamentoOSOutput();
        when(pagamentoOSService.registrar(eq(osId), any())).thenReturn(output);

        mockMvc.perform(post("/api/ordens-servico/" + osId + "/pagamento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputValido())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT deve retornar 200 ao editar pagamento")
    void editar() throws Exception {
        UUID osId = UUID.randomUUID();
        PagamentoOSOutput output = new PagamentoOSOutput();
        when(pagamentoOSService.editar(eq(osId), any())).thenReturn(output);

        mockMvc.perform(put("/api/ordens-servico/" + osId + "/pagamento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputValido())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET deve retornar 200 quando pagamento existe")
    void buscarExistente() throws Exception {
        UUID osId = UUID.randomUUID();
        when(pagamentoOSService.buscarPorOrdemServicoOpcional(osId))
                .thenReturn(Optional.of(new PagamentoOSOutput()));

        mockMvc.perform(get("/api/ordens-servico/" + osId + "/pagamento"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET deve retornar 204 quando pagamento não existe")
    void buscarInexistente() throws Exception {
        UUID osId = UUID.randomUUID();
        when(pagamentoOSService.buscarPorOrdemServicoOpcional(osId))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/ordens-servico/" + osId + "/pagamento"))
                .andExpect(status().isNoContent());
    }
}
