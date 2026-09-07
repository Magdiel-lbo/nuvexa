package com.nuvexa.nutricao.relatorio.controller;

import com.nuvexa.nutricao.model.StatusPlanoAlimentar;
import com.nuvexa.nutricao.relatorio.dto.filter.PlanoAlimentarFiltro;
import com.nuvexa.nutricao.relatorio.dto.response.PlanoAlimentarRelatorioLinhaDTO;
import com.nuvexa.nutricao.relatorio.service.PlanoAlimentarRelatorioService;
import com.nuvexa.platform.web.BaseController;
import com.nuvexa.relatorios.RelatorioResponseDTO;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/planos-alimentares/relatorio")
@RequiredArgsConstructor
public class PlanoAlimentarRelatorioController extends BaseController {

    private final PlanoAlimentarRelatorioService planoAlimentarRelatorioService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public RelatorioResponseDTO<PlanoAlimentarRelatorioLinhaDTO> generate(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) List<StatusPlanoAlimentar> status,
            @RequestParam(required = false) String plano,
            @RequestParam(required = false) List<String> faixaCalorica,
            @RequestParam(required = false) List<Integer> refeicoesPorDia) {
        return planoAlimentarRelatorioService.generate(PlanoAlimentarFiltro.of(busca, status, plano, faixaCalorica, refeicoesPorDia));
    }


    @GetMapping("/excel")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
    public ResponseEntity<byte[]> generateExcel(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) List<StatusPlanoAlimentar> status,
            @RequestParam(required = false) String plano,
            @RequestParam(required = false) List<String> faixaCalorica,
            @RequestParam(required = false) List<Integer> refeicoesPorDia) {
        byte[] excel = planoAlimentarRelatorioService.generateExcel(
                PlanoAlimentarFiltro.of(busca, status, plano, faixaCalorica, refeicoesPorDia));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"relatorio-planos-alimentares.xlsx\"")
                .body(excel);
    }
}
