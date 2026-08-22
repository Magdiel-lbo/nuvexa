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
@RequestMapping("/api/v1/patients/report")
@RequiredArgsConstructor
public class PacienteRelatorioController {

    private final PacienteRelatorioService pacienteRelatorioService;

    @GetMapping
    public RelatorioResponseDTO<PacienteRelatorioLinhaDTO> generate(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Sexo gender,
            @RequestParam(required = false) Objetivo goal,
            @RequestParam(required = false) NivelAtividade activityLevel) {
        return pacienteRelatorioService.generate(toFilter(search, gender, goal, activityLevel));
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> generateExcel(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Sexo gender,
            @RequestParam(required = false) Objetivo goal,
            @RequestParam(required = false) NivelAtividade activityLevel) {
        byte[] excel = pacienteRelatorioService.generateExcel(toFilter(search, gender, goal, activityLevel));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"relatorio-pacientes.xlsx\"")
                .body(excel);
    }

    private PacienteRelatorioFiltroDTO toFilter(String search, Sexo gender, Objetivo goal, NivelAtividade activityLevel) {
        return PacienteRelatorioFiltroDTO.builder()
                .search(search)
                .gender(gender)
                .goal(goal)
                .activityLevel(activityLevel)
                .build();
    }
}
