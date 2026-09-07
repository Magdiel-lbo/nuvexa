package com.nuvexa.nutricao.relatorio.controller;

import com.nuvexa.core.model.Sexo;
import com.nuvexa.platform.web.BaseController;
import com.nuvexa.relatorios.RelatorioResponseDTO;
import com.nuvexa.nutricao.model.NivelAtividade;
import com.nuvexa.nutricao.model.Objetivo;
import com.nuvexa.nutricao.relatorio.dto.filter.PacienteFiltro;
import com.nuvexa.nutricao.relatorio.dto.response.PacienteRelatorioLinhaDTO;
import com.nuvexa.nutricao.relatorio.service.PacienteRelatorioService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pacientes/relatorio")
@RequiredArgsConstructor
public class PacienteRelatorioController extends BaseController {

    private final PacienteRelatorioService pacienteRelatorioService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public RelatorioResponseDTO<PacienteRelatorioLinhaDTO> generate(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) Sexo sexo,
            @RequestParam(required = false) Objetivo objetivo,
            @RequestParam(required = false) NivelAtividade nivelAtividade) {
        return pacienteRelatorioService.generate(PacienteFiltro.of(busca, sexo, objetivo, nivelAtividade));
    }

    @GetMapping("/excel")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
    public ResponseEntity<byte[]> generateExcel(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) Sexo sexo,
            @RequestParam(required = false) Objetivo objetivo,
            @RequestParam(required = false) NivelAtividade nivelAtividade) {
        byte[] excel = pacienteRelatorioService.generateExcel(PacienteFiltro.of(busca, sexo, objetivo, nivelAtividade));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"relatorio-pacientes.xlsx\"")
                .body(excel);
    }
}
