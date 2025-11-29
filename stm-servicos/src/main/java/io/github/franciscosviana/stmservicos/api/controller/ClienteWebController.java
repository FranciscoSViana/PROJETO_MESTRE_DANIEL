package io.github.franciscosviana.stmservicos.api.controller;

import io.github.franciscosviana.stmservicos.api.model.input.ClienteInput;
import io.github.franciscosviana.stmservicos.common.client.model.ReceitaWsResponse;
import io.github.franciscosviana.stmservicos.domain.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ClienteWebController {

    private final ClienteService clienteService;

    @GetMapping("/clientes/form")
    public String form(Model model) {
        model.addAttribute("cliente", new ClienteInput());
        return "cliente-form";
    }

    @PostMapping("/clientes/consultar-cnpj")
    public String consultarCnpj(@RequestParam String cnpj, Model model) {
        ReceitaWsResponse receitaWsResponse = clienteService.consultarCnpj(cnpj);

        ClienteInput clienteInput = new ClienteInput();
        clienteInput.setCnpj(cnpj);
        clienteInput.setRazaoSocial(receitaWsResponse.getRazaoSocial());

        model.addAttribute("cliente", clienteInput);

        return "cliente-form";
    }

    @PostMapping("/clientes/salvar")
    public String salvar(ClienteInput clienteInput, Model model) {
        var cliente = clienteService.salvar(clienteInput);
        model.addAttribute("mensagem", "Cliente salvo com sucesso!");
        model.addAttribute("cliente", new ClienteInput());
        return "cliente-form";
    }
}
