package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.model.input.BrevoEmailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${brevo.api.url}")
    private String brevoUrl;

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.api.sender.name}")
    private String senderName;

    @Value("${brevo.api.sender.email}")
    private String senderEmail;

    private final WebClient webClient = WebClient.builder().build();

    /**
     * Envia e-mail em HTML. Use tags HTML para formatação.
     */
    public void enviarEmail(String destinatario, String assunto, String corpoHtml) {

        BrevoEmailRequest request = new BrevoEmailRequest(
                new BrevoEmailRequest.Sender(senderName, senderEmail),
                List.of(new BrevoEmailRequest.To(destinatario)),
                assunto,
                corpoHtml
        );

        try {
            webClient.post()
                    .uri(brevoUrl)
                    .header("api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            log.info("📧 Email enviado com sucesso para {}", destinatario);
        } catch (Exception e) {
            log.error("❌ Erro ao enviar email via Brevo", e);
        }
    }

    /**
     * Monta um template HTML padrão para os e-mails do sistema.
     * Garante boa renderização em qualquer cliente de e-mail.
     */
    public static String template(String titulo, String corpoHtml) {
        return """
                <!DOCTYPE html>
                <html lang="pt-BR">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin:0;padding:0;background:#f4f6f8;font-family:Arial,sans-serif;">
                  <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f4f6f8;padding:32px 0;">
                    <tr>
                      <td align="center">
                        <table width="600" cellpadding="0" cellspacing="0"
                               style="background:#ffffff;border-radius:8px;overflow:hidden;
                                      box-shadow:0 2px 8px rgba(0,0,0,0.08);max-width:600px;width:100%%;">

                          <!-- HEADER -->
                          <tr>
                            <td style="background:#1d4ed8;padding:28px 32px;text-align:center;">
                              <h1 style="margin:0;color:#ffffff;font-size:22px;font-weight:700;">
                                GUARDIAN
                              </h1>
                              <p style="margin:6px 0 0;color:#bfdbfe;font-size:13px;">%s</p>
                            </td>
                          </tr>

                          <!-- CORPO -->
                          <tr>
                            <td style="padding:32px;color:#374151;font-size:15px;line-height:1.7;">
                              %s
                            </td>
                          </tr>

                          <!-- FOOTER -->
                          <tr>
                            <td style="background:#f9fafb;padding:20px 32px;text-align:center;
                                       border-top:1px solid #e5e7eb;">
                              <p style="margin:0;color:#9ca3af;font-size:12px;">
                                Este é um e-mail automático. Por favor, não responda.
                              </p>
                            </td>
                          </tr>

                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(titulo, corpoHtml);
    }
}