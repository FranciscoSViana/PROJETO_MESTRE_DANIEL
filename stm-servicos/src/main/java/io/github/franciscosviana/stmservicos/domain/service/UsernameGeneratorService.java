package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Gera usernames únicos a partir do nome completo.
 *
 * Sequência para "Francisco Santos Viana":
 *   1ª → francisco.viana
 *   2ª → francisco.s.viana
 *   3ª → francisco.santos.viana
 *   4ª → francisco.viana2
 *   5ª → francisco.viana3 ... e assim por diante
 *
 * Roda em REQUIRES_NEW para garantir que lê o banco commitado,
 * independente da transação do chamador.
 */
@Service
@RequiredArgsConstructor
public class UsernameGeneratorService {

    private final UsuarioRepository usuarioRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String gerar(String nomeCompleto) {

        String[] partes = normalizar(nomeCompleto).split("\\s+");

        if (partes.length == 0 || partes[0].isBlank()) {
            throw new IllegalArgumentException("Nome inválido para geração de username");
        }

        String primeiro = partes[0];
        String ultimo   = partes.length > 1 ? partes[partes.length - 1] : "";
        String inicialMeio  = partes.length > 2 ? partes[1].substring(0, 1) : "";
        String nomeMeio     = partes.length > 2 ? partes[1] : "";

        // Base principal usada no fallback numérico
        String base = ultimo.isEmpty() ? primeiro : primeiro + "." + ultimo;

        // ── Carrega TODOS os usernames que começam com "primeiro" de uma vez
        //    Assim não depende de múltiplas queries dentro da mesma transação.
        Set<String> existentes = usuarioRepository
                .findByUsernameStartingWith(primeiro)
                .stream()
                .map(u -> u.getUsername().toLowerCase())
                .collect(Collectors.toSet());

        // ── Candidatos em ordem de preferência ──────────────────────────────

        // 1) primeiro.ultimo  →  francisco.viana
        String c1 = base;
        if (!existentes.contains(c1)) return c1;

        // 2) primeiro.inicialMeio.ultimo  →  francisco.s.viana
        if (!inicialMeio.isEmpty()) {
            String c2 = primeiro + "." + inicialMeio + "." + ultimo;
            if (!existentes.contains(c2)) return c2;
        }

        // 3) primeiro.nomeMeio.ultimo  →  francisco.santos.viana
        if (!nomeMeio.isEmpty() && !nomeMeio.equals(inicialMeio)) {
            String c3 = primeiro + "." + nomeMeio + "." + ultimo;
            if (!existentes.contains(c3)) return c3;
        }

        // 4) primeiro.ultimo + sufixo numérico crescente a partir de 2
        long sufixo = 2;
        String candidato;
        do {
            candidato = base + sufixo;
            sufixo++;
        } while (existentes.contains(candidato)
                || usuarioRepository.existsByUsername(candidato)); // double-check no banco

        return candidato;
    }

    /** Remove acentos, converte para minúsculas e descarta não-alfa. */
    private String normalizar(String texto) {
        return Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase()
                .replaceAll("[^a-z\\s]", "")
                .replaceAll("\\s+", " "); // colapsa espaços múltiplos
    }
}