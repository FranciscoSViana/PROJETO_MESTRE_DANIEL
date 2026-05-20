package io.github.franciscosviana.stmservicos.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.franciscosviana.stmservicos.api.exceptionhandler.ApiExceptionHandler;
import io.github.franciscosviana.stmservicos.api.model.input.PagamentoClienteOSInput;
import io.github.franciscosviana.stmservicos.api.model.output.PagamentoClienteOSOutput;
import io.github.franciscosviana.stmservicos.common.validation.OrdemServicoException;
import io.github.franciscosviana.stmservicos.config.TestSecurityConfig;
import io.github.franciscosviana.stmservicos.domain.service.PagamentoClienteOSService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PagamentoClienteOSController.class)
@DisplayName("PagamentoClienteOSController (MockMvc)")
@Import({TestSecurityConfig.class, ApiExceptionHandler.class})
class PagamentoClienteOSControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    PagamentoClienteOSService pagamentoClienteOSService;

    private final UUID osId = UUID.randomUUID();

    @Nested
    @DisplayName("POST /api/ordens-servico/{ordemServicoId}/recebimento")
    class Registrar {

        @Test
        @DisplayName("deve retornar 200 ao registrar recebimento com sucesso")
        void deveRetornar200AoRegistrar() throws Exception {
            PagamentoClienteOSOutput output = pagamentoOutput();
            when(pagamentoClienteOSService.registrar(any(), any())).thenReturn(output);

            mockMvc.perform(post("/api/ordens-servico/{ordemServicoId}/recebimento", osId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputPadrao())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.osg").value("OSG260001"))
                    .andExpect(jsonPath("$.valorTotal").value(175.00));
        }

        @Test
        @DisplayName("deve retornar 500 quando OS lança OrdemServicoException não mapeada")
        void deveRetornar500SeOsSemSolucao() throws Exception {
            // OrdemServicoException não tem mapeamento próprio no ApiExceptionHandler,
            // cai no handler genérico (Exception.class) que retorna 500.
            when(pagamentoClienteOSService.registrar(any(), any()))
                    .thenThrow(new OrdemServicoException("A OS ainda não possui solução registrada."));

            mockMvc.perform(post("/api/ordens-servico/{ordemServicoId}/recebimento", osId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputPadrao())))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("PUT /api/ordens-servico/{ordemServicoId}/recebimento")
    class Editar {

        @Test
        @DisplayName("deve retornar 200 ao editar recebimento com sucesso")
        void deveRetornar200AoEditar() throws Exception {
            PagamentoClienteOSOutput output = pagamentoOutput();
            when(pagamentoClienteOSService.editar(any(), any())).thenReturn(output);

            mockMvc.perform(put("/api/ordens-servico/{ordemServicoId}/recebimento", osId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(inputPadrao())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.osg").value("OSG260001"));
        }
    }

    @Nested
    @DisplayName("GET /api/ordens-servico/{ordemServicoId}/recebimento")
    class Buscar {

        @Test
        @DisplayName("deve retornar 200 com recebimento quando encontrado")
        void deveRetornar200QuandoEncontrado() throws Exception {
            when(pagamentoClienteOSService.buscarPorOrdemServicoOpcional(any()))
                    .thenReturn(Optional.of(pagamentoOutput()));

            mockMvc.perform(get("/api/ordens-servico/{ordemServicoId}/recebimento", osId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.osg").value("OSG260001"));
        }

        @Test
        @DisplayName("deve retornar 204 quando recebimento não encontrado")
        void deveRetornar204QuandoNaoEncontrado() throws Exception {
            when(pagamentoClienteOSService.buscarPorOrdemServicoOpcional(any()))
                    .thenReturn(Optional.empty());

            mockMvc.perform(get("/api/ordens-servico/{ordemServicoId}/recebimento", osId))
                    .andExpect(status().isNoContent());
        }
    }

    private PagamentoClienteOSInput inputPadrao() {
        PagamentoClienteOSInput input = new PagamentoClienteOSInput();
        input.setValorChamado(new BigDecimal("150.00"));
        input.setKm(new BigDecimal("10"));
        input.setValorKm(new BigDecimal("2.50"));
        input.setPedagio(BigDecimal.ZERO);
        input.setEstacionamento(BigDecimal.ZERO);
        input.setValorOutros(BigDecimal.ZERO);
        input.setDataPagamento(LocalDate.now());
        return input;
    }

    private PagamentoClienteOSOutput pagamentoOutput() {
        PagamentoClienteOSOutput output = new PagamentoClienteOSOutput();
        output.setOsg("OSG260001");
        output.setValorTotal(new BigDecimal("175.00"));
        return output;
    }
}
