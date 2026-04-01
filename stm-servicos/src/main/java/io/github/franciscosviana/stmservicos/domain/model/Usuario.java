package io.github.franciscosviana.stmservicos.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue
    @Column(columnDefinition = "uuid")
    private UUID id;

    /** Nome completo do usuário (ex: Francisco Santos Viana) */
    @Column(name = "nome_completo")
    private String nomeCompleto;

    /**
     * Username gerado automaticamente a partir do nome completo.
     * Usado para login. Ex: francisco.viana, francisco.s.viana
     */
    @Column(unique = true, nullable = false)
    private String username;

    /**
     * Campo "nome" mantido por compatibilidade — passa a armazenar
     * o mesmo valor que username após a migração.
     */
    @Column(nullable = false)
    private String nome;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(name = "role_name")
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_roles", joinColumns = @JoinColumn(name = "usuario_id"))
    private Set<String> roles;

    private boolean enabled = true;

    private Instant createdAt;

    /** Data/hora da última troca de senha — base para alertas trimestrais */
    @Column(name = "ultima_alteracao_senha")
    private Instant ultimaAlteracaoSenha;

    /** Controle para não reenviar e-mail de aviso mais de uma vez por ciclo */
    @Column(name = "notificacao_senha_enviada")
    private boolean notificacaoSenhaEnviada = false;

    /** Calcula a idade em anos a partir da data de nascimento */
    @Transient
    public Integer getIdade() {
        if (dataNascimento == null) return null;
        return Period.between(dataNascimento, LocalDate.now()).getYears();
    }
}