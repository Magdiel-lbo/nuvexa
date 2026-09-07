package com.nuvexa.core.relatorio.controller;

import com.nuvexa.core.model.SecaoProntuario;
import com.nuvexa.core.model.StatusProntuario;
import com.nuvexa.core.relatorio.dto.filter.ProntuarioFiltro;
import com.nuvexa.core.relatorio.dto.response.ProntuarioRelatorioLinhaDTO;
import com.nuvexa.core.relatorio.service.ProntuarioRelatorioService;
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
@RequestMapping("/api/v1/prontuarios/relatorio")
@RequiredArgsConstructor
public class ProntuarioRelatorioController extends BaseController {

    private final ProntuarioRelatorioService prontuarioRelatorioService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public RelatorioResponseDTO<ProntuarioRelatorioLinhaDTO> generate(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) List<StatusProntuario> status,
            @RequestParam(required = false) List<SecaoProntuario> secao,
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) List<Long> autorId,
            @RequestParam(required = false) List<String> anexo) {
        return prontuarioRelatorioService.generate(ProntuarioFiltro.of(busca, status, secao, periodo, autorId, anexo));
    }

    /* Mesma exceção documentada em PacienteRelatorioController: o content-type real (xlsx) só é
     * definido em runtime, o @ApiResponse abaixo só ajusta o media type da resposta 200 já
     * inferida pelo Springdoc. */
    @GetMapping("/excel")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponse(responseCode = "200", content = @Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
    public ResponseEntity<byte[]> generateExcel(
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) List<StatusProntuario> status,
            @RequestParam(required = false) List<SecaoProntuario> secao,
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) List<Long> autorId,
            @RequestParam(required = false) List<String> anexo) {
        byte[] excel = prontuarioRelatorioService.generateExcel(ProntuarioFiltro.of(busca, status, secao, periodo, autorId, anexo));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"relatorio-prontuarios.xlsx\"")
                .body(excel);
    }
}
