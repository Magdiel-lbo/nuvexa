package com.nuvexa.platform.auditoria;

import com.nuvexa.platform.persistence.ModeloAbstrato;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Log de auditoria genérico e reutilizável por qualquer entidade clínica — por isso vive em
 * platform/, não em core/. Referencia a entidade auditada por {@code entidadeTipo}+{@code
 * entidadeId} (sem FK real, já que é polimórfico por natureza) e {@code organizacaoId}/{@code
 * usuarioId} como colunas simples (não {@code @ManyToOne}) para não criar dependência de
 * platform/ para core/ — a integridade referencial dessas duas colunas fica garantida por FK no
 * banco (ver migration), só não é modelada como relação JPA. {@code usuarioNome} é um snapshot
 * congelado no momento do evento, para a trilha não mudar se o usuário for renomeado depois.
 * Nunca é atualizado nem excluído — {@link AuditoriaService} só expõe {@code registrar}/{@code
 * listar}.
 */
@Entity
@Table(name = "eventos_auditoria")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class EventoAuditoria extends ModeloAbstrato {

    @Column(name = "organizacao_id", nullable = false)
    private Long organizacaoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entidade_tipo", nullable = false, length = 30)
    private EntidadeAuditavel entidadeTipo;

    @Column(name = "entidade_id", nullable = false)
    private Long entidadeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false, length = 20)
    private TipoEventoAuditoria tipoEvento;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "usuario_nome", nullable = false, length = 150)
    private String usuarioNome;

    @Column(name = "dados_antes", columnDefinition = "TEXT")
    private String dadosAntes;

    @Column(name = "dados_depois", columnDefinition = "TEXT")
    private String dadosDepois;
}
