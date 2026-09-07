package com.nuvexa.core.relatorio.controller;

import com.nuvexa.core.model.StatusConsulta;
import com.nuvexa.core.model.TipoConsulta;
import com.nuvexa.core.relatorio.dto.filter.ConsultaFiltro;
import com.nuvexa.core.relatorio.dto.response.ConsultaRelatorioLinhaDTO;
import com.nuvexa.core.relatorio.service.ConsultaRelatorioService;
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
@RequestMapping("/api/v1/consultas/relatorio")
@RequiredArgsConstructor
public class ConsultaRelatorioController extends BaseController {

    private final ConsultaRelatorioService consultaRelatorioService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public RelatorioResponseDTO<ConsultaRelatorioLinhaDTO> generate(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) List<StatusConsulta> status,
            @RequestParam(required = false) List<TipoConsulta> tipo,
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) List<Long> profissionalId) {
        return consultaRelatorioService.generate(ConsultaFiltro.of(busca, status, tipo, periodo, profissionalId));
    }

    /* Mesma exceção documentada em PacienteRelatorioController: o content-type real (xlsx) só é
     * definido em runtime, o @ApiResponse abaixo só ajusta o media type da resposta 200 já
     * inferida pelo Springdoc. */
    @GetMapping("/excel")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
    public ResponseEntity<byte[]> generateExcel(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) List<StatusConsulta> status,
            @RequestParam(required = false) List<TipoConsulta> tipo,
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) List<Long> profissionalId) {
        byte[] excel = consultaRelatorioService.generateExcel(ConsultaFiltro.of(busca, status, tipo, periodo, profissionalId));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"relatorio-consultas.xlsx\"")
                .body(excel);
    }
}
