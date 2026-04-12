package io.github.franciscosviana.stmservicos.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("EmailService e S3Service")
class EmailAndS3ServiceTest {

    // ──────────────────────────────────────────────────────────────────────────
    // EmailService.template() — método estático
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("EmailService.template()")
    class EmailTemplate {

        @Test
        @DisplayName("deve retornar HTML válido contendo título e corpo")
        void deveRetornarHtmlComTituloECorpo() {
            String html = EmailService.template("Meu Título", "<p>Corpo do email</p>");

            assertThat(html).contains("Meu Título");
            assertThat(html).contains("<p>Corpo do email</p>");
            assertThat(html).contains("<!DOCTYPE html>");
            assertThat(html).contains("GUARDIAN");
        }

        @Test
        @DisplayName("deve incluir meta charset UTF-8")
        void deveIncluirCharset() {
            String html = EmailService.template("T", "C");
            assertThat(html).contains("UTF-8");
        }

        @Test
        @DisplayName("deve incluir viewport para responsividade")
        void deveIncluirViewport() {
            String html = EmailService.template("T", "C");
            assertThat(html).contains("viewport");
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // S3Service
    // ──────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("S3Service")
    @ExtendWith(MockitoExtension.class)
    class S3ServiceTest {

        @Mock
        S3Client s3Client;
        @InjectMocks
        S3Service service;

        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(service, "bucket", "meu-bucket");
        }

        @Test
        @DisplayName("upload() deve retornar URL pública no formato correto")
        void deveRetornarUrlPublica() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "comprovante", "recibo.pdf", "application/pdf",
                    "conteudo-pdf".getBytes());

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(null);

            String url = service.upload(file, "OSG260001");

            assertThat(url).startsWith("https://meu-bucket.s3.amazonaws.com/");
            assertThat(url).contains("OSG260001");
            assertThat(url).endsWith(".pdf");
        }

        @Test
        @DisplayName("upload() deve usar extensão do arquivo original")
        void deveUsarExtensaoDoArquivo() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "img", "foto.jpg", "image/jpeg", "bytes".getBytes());

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(null);

            String url = service.upload(file, "OSG260002");
            assertThat(url).endsWith(".jpg");
        }

        @Test
        @DisplayName("deletar() deve extrair a key da URL e chamar deleteObject")
        void deveDeletarArquivo() {
            String url = "https://meu-bucket.s3.amazonaws.com/comprovantes/OSG260001.pdf";

            service.deletar(url);

            verify(s3Client).deleteObject(any(
                    software.amazon.awssdk.services.s3.model.DeleteObjectRequest.class));
        }

        @Test
        @DisplayName("deletar() deve lançar RuntimeException quando URL não pertence ao bucket")
        void deveLancarExcecaoUrlInvalida() {
            assertThatThrownBy(() -> service.deletar("https://outro-bucket.s3.amazonaws.com/key"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("bucket configurado");
        }
    }
}
