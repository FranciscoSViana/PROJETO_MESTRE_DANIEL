package io.github.franciscosviana.stmservicos.api.controller;

import io.github.franciscosviana.stmservicos.api.exceptionhandler.ApiExceptionHandler;
import io.github.franciscosviana.stmservicos.config.TestSecurityConfig;
import io.github.franciscosviana.stmservicos.domain.service.S3Service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UploadController.class)
@DisplayName("UploadController (MockMvc)")
@Import({TestSecurityConfig.class, ApiExceptionHandler.class})
class UploadControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    S3Service s3Service;

    @Nested
    @DisplayName("POST /api/uploads/comprovante")
    class UploadComprovante {

        @Test
        @DisplayName("deve retornar 200 com URL do comprovante enviado")
        void deveRetornar200ComUrl() throws Exception {
            String url = "https://bucket.s3.amazonaws.com/comprovantes/OSG260001.pdf";
            when(s3Service.upload(any(), any())).thenReturn(url);

            MockMultipartFile arquivo = new MockMultipartFile(
                    "file", "comprovante.pdf",
                    "application/pdf", "conteudo".getBytes());

            mockMvc.perform(multipart("/api/uploads/comprovante")
                            .file(arquivo)
                            .param("osg", "OSG260001"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(url));
        }
    }

    @Nested
    @DisplayName("POST /api/uploads/recebimento")
    class UploadRecebimento {

        @Test
        @DisplayName("deve retornar 200 com URL do recebimento enviado")
        void deveRetornar200ComUrl() throws Exception {
            String url = "https://bucket.s3.amazonaws.com/recebimentos/pagamento-cliente-OSG260001.pdf";
            when(s3Service.uploadRecebimento(any(), any())).thenReturn(url);

            MockMultipartFile arquivo = new MockMultipartFile(
                    "file", "recebimento.pdf",
                    "application/pdf", "conteudo".getBytes());

            mockMvc.perform(multipart("/api/uploads/recebimento")
                            .file(arquivo)
                            .param("osg", "OSG260001"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(url));
        }
    }
}
