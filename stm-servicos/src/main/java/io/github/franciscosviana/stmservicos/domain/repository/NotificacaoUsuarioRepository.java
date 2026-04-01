package io.github.franciscosviana.stmservicos.domain.repository;

import io.github.franciscosviana.stmservicos.domain.model.NotificacaoUsuario;
import io.github.franciscosviana.stmservicos.domain.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface NotificacaoUsuarioRepository extends JpaRepository<NotificacaoUsuario, UUID> {

    /**
     * Notificações não lidas de um usuário, mais recentes primeiro
     */
    List<NotificacaoUsuario> findByUsuarioAndLidaFalseOrderByCriadaEmDesc(Usuario usuario);

    /**
     * Todas as notificações de um usuário (para histórico)
     */
    List<NotificacaoUsuario> findByUsuarioOrderByCriadaEmDesc(Usuario usuario);

    /**
     * Marca todas como lidas para um usuário
     */
    @Modifying
    @Query("UPDATE NotificacaoUsuario n SET n.lida = true WHERE n.usuario = :usuario")
    void marcarTodasComoLidas(Usuario usuario);
}