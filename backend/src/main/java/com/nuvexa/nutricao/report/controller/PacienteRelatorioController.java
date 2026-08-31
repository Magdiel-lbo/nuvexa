package com.nuvexa.nutricao.report.controller;

import com.nuvexa.core.paciente.model.Sexo;
import com.nuvexa.relatorios.RelatorioResponseDTO;
import com.nuvexa.nutricao.model.NivelAtividade;
import com.nuvexa.nutricao.model.Objetivo;
import com.nuvexa.nutricao.report.dto.request.PacienteRelatorioFiltroDTO;
import com.nuvexa.nutricao.report.dto.response.PacienteRelatorioLinhaDTO;
import com.nuvexa.nutricao.report.service.PacienteRelatorioService;
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
        return pacienteRelatorioService.generate(PacienteRelatorioFiltroDTO.of(busca, sexo, objetivo, nivelAtividade));
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> generateExcel(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) Sexo sexo,
            @RequestParam(required = false) Objetivo objetivo,
            @RequestParam(required = false) NivelAtividade nivelAtividade) {
        byte[] excel = pacienteRelatorioService.generateExcel(PacienteRelatorioFiltroDTO.of(busca, sexo, objetivo, nivelAtividade));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"relatorio-pacientes.xlsx\"")
                .body(excel);
    }
}
