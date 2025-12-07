package io.github.franciscosviana.stmservicos.domain.repository;

import io.github.franciscosviana.stmservicos.domain.model.HistoricoSenha;
import io.github.franciscosviana.stmservicos.domain.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HistoricoSenhaRepository extends JpaRepository<HistoricoSenha, UUID> {

    void deleteAllByUsuarioId(Usuario usuario);
    List<HistoricoSenha> findTop5ByUsuarioIdOrderByCriadaEmDesc(Usuario usuarioId);
}
