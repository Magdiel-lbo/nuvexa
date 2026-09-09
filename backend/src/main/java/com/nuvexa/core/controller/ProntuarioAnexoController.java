package com.nuvexa.core.controller;

import com.nuvexa.core.dto.response.ProntuarioAnexoDownloadDTO;
import com.nuvexa.core.dto.response.ProntuarioAnexoResponseDTO;
import com.nuvexa.core.service.ProntuarioAnexoService;
import com.nuvexa.platform.web.BaseController;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/prontuarios/{prontuarioId}/anexos")
@RequiredArgsConstructor
public class ProntuarioAnexoController extends BaseController {

    private final ProntuarioAnexoService prontuarioAnexoService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProntuarioAnexoResponseDTO> findAll(@PathVariable Long prontuarioId) {
        return prontuarioAnexoService.findAll(prontuarioId);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ProntuarioAnexoResponseDTO upload(@PathVariable Long prontuarioId, @RequestParam("arquivo") MultipartFile arquivo) {
        return prontuarioAnexoService.upload(prontuarioId, arquivo);
    }

    @GetMapping("/{anexoId}/download")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<byte[]> download(@PathVariable Long prontuarioId, @PathVariable Long anexoId) {
        ProntuarioAnexoDownloadDTO arquivo = prontuarioAnexoService.download(prontuarioId, anexoId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(arquivo.getTipoMime()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + arquivo.getNomeOriginal() + "\"")
                .body(arquivo.getConteudo());
    }

    @DeleteMapping("/{anexoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long prontuarioId, @PathVariable Long anexoId) {
        prontuarioAnexoService.delete(prontuarioId, anexoId);
    }
}
