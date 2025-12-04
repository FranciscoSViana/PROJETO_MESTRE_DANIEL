package io.github.franciscosviana.stmservicos.api.controller;

import io.github.franciscosviana.stmservicos.domain.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teste-email")
@RequiredArgsConstructor
public class TesteEmailController {

    private final EmailService emailService;

    @GetMapping
    public String enviarTeste() {
        emailService.enviarEmail("franciscosantos29@gmail.com", "Teste de Email", "Olá! Este é um teste.");
        return "Email enviado!";
    }
}
