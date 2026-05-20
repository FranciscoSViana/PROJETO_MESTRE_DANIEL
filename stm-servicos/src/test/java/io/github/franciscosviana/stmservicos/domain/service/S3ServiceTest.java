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
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Nested
    @DisplayName("upload()")
    class Upload {

        @Test
        @DisplayName("deve retornar URL pública no formato correto")
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
        @DisplayName("deve usar extensão .pdf para arquivos PDF")
        void deveUsarExtensaoPdf() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "comprovante", "recibo.pdf", "application/pdf",
                    "bytes".getBytes());

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(null);

            String url = service.upload(file, "OSG260001");
            assertThat(url).endsWith(".pdf");
        }

        @Test
        @DisplayName("deve usar extensão .jpg para imagens")
        void deveUsarExtensaoImagem() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "img", "foto.jpg", "image/jpeg", "bytes".getBytes());

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(null);

            String url = service.upload(file, "OSG260002");
            assertThat(url).endsWith(".jpg");
        }

        @Test
        @DisplayName("deve armazenar na pasta comprovantes/")
        void deveUsarPastaComprovantes() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "comp", "comp.pdf", "application/pdf", "bytes".getBytes());

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(null);

            String url = service.upload(file, "OSG260001");
            assertThat(url).contains("comprovantes/");
        }
    }

    @Nested
    @DisplayName("uploadRecebimento()")
    class UploadRecebimento {

        @Test
        @DisplayName("deve retornar URL na pasta recebimentos/")
        void deveRetornarUrlRecebimento() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "recebimento", "rec.pdf", "application/pdf", "bytes".getBytes());

            when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                    .thenReturn(null);

            String url = service.uploadRecebimento(file, "OSG260001");

            assertThat(url).startsWith("https://meu-bucket.s3.amazonaws.com/");
            assertThat(url).contains("recebimentos/");
            assertThat(url).contains("OSG260001");
        }
    }

    @Nested
    @DisplayName("deletar()")
    class Deletar {

        @Test
        @DisplayName("deve extrair a key da URL e chamar deleteObject")
        void deveDeletarArquivo() {
            String url = "https://meu-bucket.s3.amazonaws.com/comprovantes/OSG260001.pdf";

            service.deletar(url);

            verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
        }

        @Test
        @DisplayName("deve lançar RuntimeException quando URL não pertence ao bucket")
        void deveLancarExcecaoUrlInvalida() {
            assertThatThrownBy(() -> service.deletar("https://outro-bucket.s3.amazonaws.com/key"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("bucket configurado");
        }

        @Test
        @DisplayName("deve funcionar para arquivos na pasta recebimentos/")
        void deveDeletarRecebimento() {
            String url = "https://meu-bucket.s3.amazonaws.com/recebimentos/pagamento-cliente-OSG260001.pdf";

            service.deletar(url);

            verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
        }
    }
}
