package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.domain.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;

/**
 * Gera usernames únicos a partir do nome completo.
 *
 * Exemplos para "Francisco Santos Viana":
 *   1ª tentativa → francisco.viana
 *   2ª tentativa → francisco.s.viana
 *   3ª em diante → francisco.viana2, francisco.viana3...
 */
@Service
@RequiredArgsConstructor
public class UsernameGeneratorService {

    private final UsuarioRepository usuarioRepository;

    public String gerar(String nomeCompleto) {
        String[] partes = normalizar(nomeCompleto).split("\\s+");

        if (partes.length == 0) throw new IllegalArgumentException("Nome inválido");

        String primeiro = partes[0];
        String ultimo   = partes.length > 1 ? partes[partes.length - 1] : "";
        String meio     = partes.length > 2 ? partes[1].substring(0, 1) : "";

        // Candidatos em ordem de preferência
        String base1 = primeiro + (ultimo.isEmpty() ? "" : "." + ultimo);           // francisco.viana
        String base2 = primeiro + (meio.isEmpty() ? "" : "." + meio) + (ultimo.isEmpty() ? "" : "." + ultimo); // francisco.s.viana

        if (!usuarioRepository.existsByUsername(base1)) return base1;
        if (!base2.equals(base1) && !usuarioRepository.existsByUsername(base2)) return base2;

        // Fallback: adiciona sufixo numérico ao base1
        String prefix = base1;
        List<io.github.franciscosviana.stmservicos.domain.model.Usuario> existentes =
                usuarioRepository.findByUsernameStartingWith(prefix);

        long count = existentes.stream()
                .filter(u -> u.getUsername().matches(prefix.replace(".", "\\.") + "\\d*"))
                .count();

        String candidato;
        long sufixo = count + 1;
        do {
            candidato = prefix + sufixo;
            sufixo++;
        } while (usuarioRepository.existsByUsername(candidato));

        return candidato;
    }

    /** Remove acentos e converte para minúsculas */
    private String normalizar(String texto) {
        String semAcento = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return semAcento.toLowerCase().replaceAll("[^a-z\\s]", "");
    }
}