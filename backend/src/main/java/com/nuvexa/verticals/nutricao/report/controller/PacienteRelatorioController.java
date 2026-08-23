package com.nuvexa.verticals.nutricao.report.controller;

import com.nuvexa.core.paciente.model.Sexo;
import com.nuvexa.platform.report.RelatorioResponseDTO;
import com.nuvexa.verticals.nutricao.model.NivelAtividade;
import com.nuvexa.verticals.nutricao.model.Objetivo;
import com.nuvexa.verticals.nutricao.report.dto.request.PacienteRelatorioFiltroDTO;
import com.nuvexa.verticals.nutricao.report.dto.response.PacienteRelatorioLinhaDTO;
import com.nuvexa.verticals.nutricao.report.service.PacienteRelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pacientes/relatorio")
@RequiredArgsConstructor
public class PacienteRelatorioController {

    private final PacienteRelatorioService pacienteRelatorioService;

    @GetMapping
    public RelatorioResponseDTO<PacienteRelatorioLinhaDTO> generate(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) Sexo sexo,
            @RequestParam(required = false) Objetivo objetivo,
            @RequestParam(required = false) NivelAtividade nivelAtividade) {
        return pacienteRelatorioService.generate(toFiltro(busca, sexo, objetivo, nivelAtividade));
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> generateExcel(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) Sexo sexo,
            @RequestParam(required = false) Objetivo objetivo,
            @RequestParam(required = false) NivelAtividade nivelAtividade) {
        byte[] excel = pacienteRelatorioService.generateExcel(toFiltro(busca, sexo, objetivo, nivelAtividade));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"relatorio-pacientes.xlsx\"")
                .body(excel);
    }

    private PacienteRelatorioFiltroDTO toFiltro(String busca, Sexo sexo, Objetivo objetivo, NivelAtividade nivelAtividade) {
        return PacienteRelatorioFiltroDTO.builder()
                .busca(busca)
                .sexo(sexo)
                .objetivo(objetivo)
                .nivelAtividade(nivelAtividade)
                .build();
    }
}
