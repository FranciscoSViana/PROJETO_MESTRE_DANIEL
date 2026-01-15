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

    public void enviarEmail(String destinatario, String assunto, String mensagem) {

        BrevoEmailRequest request = new BrevoEmailRequest(
                new BrevoEmailRequest.Sender(senderName, senderEmail),
                List.of(new BrevoEmailRequest.To(destinatario)),
                assunto,
                mensagem
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
}

