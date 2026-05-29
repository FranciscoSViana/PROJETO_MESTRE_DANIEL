package io.github.franciscosviana.stmservicos.api.controller;

import io.github.franciscosviana.stmservicos.config.TestSecurityConfig;
import io.github.franciscosviana.stmservicos.domain.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser
@Import(TestSecurityConfig.class)
@WebMvcTest(TesteEmailController.class)
@DisplayName("TesteEmailController (MockMvc)")
class TesteEmailControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    EmailService emailService;

    @Test
    @DisplayName("GET /teste-email deve enviar email e retornar mensagem de sucesso")
    void deveEnviarTesteEmail() throws Exception {
        doNothing().when(emailService).enviarEmail(any(), any(), any());

        mockMvc.perform(get("/teste-email"))
                .andExpect(status().isOk())
                .andExpect(content().string("Email enviado!"));
    }
}