package com.nuvexa.core.model;

import com.nuvexa.platform.persistence.ModeloAbstrato;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

/**
 * Convite para uma pessoa sem conta entrar em uma {@link Organizacao} com um {@link PapelOrganizacional}.
 * Atrelado ao e-mail, uso único e com validade; o token em claro só existe no e-mail enviado.
 */
@Entity
@Table(name = "convites")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Convite extends ModeloAbstrato {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizacao_id", nullable = false)
    private Organizacao organizacao;

    @Column(nullable = false, length = 150)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PapelOrganizacional papel;

    @Column(name = "hash_token", nullable = false, length = 64)
    private String hashToken;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "convidado_por_id", nullable = false)
    private Usuario convidadoPor;

    @Column(name = "usado_em")
    private LocalDateTime usadoEm;

    @Column(name = "revogado_em")
    private LocalDateTime revogadoEm;

    public StatusConvite status(LocalDateTime agora) {
        if (usadoEm != null) {
            return StatusConvite.ACEITO;
        }
        if (revogadoEm != null) {
            return StatusConvite.REVOGADO;
        }
        return expiraEm.isAfter(agora) ? StatusConvite.PENDENTE : StatusConvite.EXPIRADO;
    }
}
