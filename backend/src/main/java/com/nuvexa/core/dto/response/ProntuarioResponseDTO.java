package com.nuvexa.core.dto.response;

import com.nuvexa.core.model.Prontuario;
import com.nuvexa.core.model.SecaoProntuario;
import com.nuvexa.core.model.StatusProntuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProntuarioResponseDTO {

    private Long id;

    private String registro;

    private Long pacienteId;

    private String pacienteNome;

    private Long autorId;

    private String autorNome;

    private SecaoProntuario secao;

    private StatusProntuario status;

    private String conteudo;

    private boolean comAnexo;

    private Long assinadoPorId;

    private String assinadoPorNome;

    private LocalDateTime assinadoEm;

    private LocalDateTime criadoEm;

    private LocalDateTime atualizadoEm;

    public static ProntuarioResponseDTO from(Prontuario prontuario) {
        return ProntuarioResponseDTO.builder()
                .id(prontuario.getId())
                .registro("PR-" + String.format("%04d", prontuario.getId()))
                .pacienteId(prontuario.getPaciente().getId())
                .pacienteNome(prontuario.getPaciente().getNome())
                .autorId(prontuario.getAutor().getId())
                .autorNome(prontuario.getAutor().getNome())
                .secao(prontuario.getSecao())
                .status(prontuario.getStatus())
                .conteudo(prontuario.getConteudo())
                .comAnexo(prontuario.isComAnexo())
                .assinadoPorId(prontuario.getAssinadoPor() != null ? prontuario.getAssinadoPor().getId() : null)
                .assinadoPorNome(prontuario.getAssinadoPor() != null ? prontuario.getAssinadoPor().getNome() : null)
                .assinadoEm(prontuario.getAssinadoEm())
                .criadoEm(prontuario.getCriadoEm())
                .atualizadoEm(prontuario.getAtualizadoEm())
                .build();
    }
}
