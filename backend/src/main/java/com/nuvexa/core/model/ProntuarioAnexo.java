package com.nuvexa.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Documento anexado a um {@link Prontuario} (exame, laudo etc.). Igual a
 * {@link ProntuarioAdendo}, não estende {@link com.nuvexa.platform.persistence.ModeloAbstrato} de
 * propósito: é imutável desde a criação, não existe fluxo de edição. {@code chaveStorage} é o
 * identificador físico no storage (gerado pelo backend, nunca o nome original do arquivo);
 * {@code nomeOriginal} existe só como metadado de exibição/download.
 */
@Entity
@Table(name = "prontuario_anexos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProntuarioAnexo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizacao_id", nullable = false)
    private Organizacao organizacao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prontuario_id", nullable = false)
    private Prontuario prontuario;

    @Column(name = "nome_original", nullable = false, length = 255)
    private String nomeOriginal;

    @Column(name = "chave_storage", nullable = false, unique = true, length = 255)
    private String chaveStorage;

    @Column(name = "tipo_mime", nullable = false, length = 100)
    private String tipoMime;

    @Column(nullable = false)
    private Long tamanho;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "criado_por_id", nullable = false)
    private Usuario criadoPor;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
    }
}
