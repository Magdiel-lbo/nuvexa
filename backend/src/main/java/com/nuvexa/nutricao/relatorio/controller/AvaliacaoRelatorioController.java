package com.nuvexa.nutricao.relatorio.controller;

import com.nuvexa.nutricao.model.StatusAvaliacao;
import com.nuvexa.nutricao.model.TipoAvaliacao;
import com.nuvexa.nutricao.relatorio.dto.filter.AvaliacaoFiltro;
import com.nuvexa.nutricao.relatorio.dto.response.AvaliacaoRelatorioLinhaDTO;
import com.nuvexa.nutricao.relatorio.service.AvaliacaoRelatorioService;
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
@RequestMapping("/api/v1/avaliacoes/relatorio")
@RequiredArgsConstructor
public class AvaliacaoRelatorioController extends BaseController {

    private final AvaliacaoRelatorioService avaliacaoRelatorioService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public RelatorioResponseDTO<AvaliacaoRelatorioLinhaDTO> generate(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) List<TipoAvaliacao> tipo,
            @RequestParam(required = false) List<StatusAvaliacao> status,
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) List<Long> avaliadorId,
            @RequestParam(required = false) List<String> tendencia) {
        return avaliacaoRelatorioService.generate(AvaliacaoFiltro.of(busca, tipo, status, periodo, avaliadorId, tendencia));
    }

    @GetMapping("/excel")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
    public ResponseEntity<byte[]> generateExcel(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) List<TipoAvaliacao> tipo,
            @RequestParam(required = false) List<StatusAvaliacao> status,
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) List<Long> avaliadorId,
            @RequestParam(required = false) List<String> tendencia) {
        byte[] excel = avaliacaoRelatorioService.generateExcel(
                AvaliacaoFiltro.of(busca, tipo, status, periodo, avaliadorId, tendencia));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"relatorio-avaliacoes.xlsx\"")
                .body(excel);
    }
}
