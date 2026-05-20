package io.github.franciscosviana.stmservicos.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EmailService")
class EmailServiceTest {

    @Nested
    @DisplayName("template()")
    class Template {

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

        @Test
        @DisplayName("deve retornar string não nula e não vazia")
        void deveRetornarStringNaoVazia() {
            String html = EmailService.template("Assunto", "Mensagem");
            assertThat(html).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("deve ter estrutura de documento HTML completa")
        void deveSerDocumentoHtmlCompleto() {
            String html = EmailService.template("Teste", "Conteúdo");
            assertThat(html).contains("<html").contains("</html>");
            assertThat(html).contains("<body").contains("</body>");
        }
    }
}
