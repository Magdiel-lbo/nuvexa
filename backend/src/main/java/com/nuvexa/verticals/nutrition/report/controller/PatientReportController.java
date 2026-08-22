package com.nuvexa.verticals.nutrition.report.controller;

import com.nuvexa.core.patient.model.Gender;
import com.nuvexa.platform.report.ReportResponseDTO;
import com.nuvexa.verticals.nutrition.model.ActivityLevel;
import com.nuvexa.verticals.nutrition.model.Goal;
import com.nuvexa.verticals.nutrition.report.dto.request.PatientReportFilterDTO;
import com.nuvexa.verticals.nutrition.report.dto.response.PatientReportRowDTO;
import com.nuvexa.verticals.nutrition.report.service.PatientReportService;
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
public class PatientReportController {

    private final PatientReportService patientReportService;

    @GetMapping
    public ReportResponseDTO<PatientReportRowDTO> generate(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) Goal goal,
            @RequestParam(required = false) ActivityLevel activityLevel) {
        return patientReportService.generate(toFilter(search, gender, goal, activityLevel));
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> generateExcel(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Gender gender,
            @RequestParam(required = false) Goal goal,
            @RequestParam(required = false) ActivityLevel activityLevel) {
        byte[] excel = patientReportService.generateExcel(toFilter(search, gender, goal, activityLevel));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"relatorio-pacientes.xlsx\"")
                .body(excel);
    }

    private PatientReportFilterDTO toFilter(String search, Gender gender, Goal goal, ActivityLevel activityLevel) {
        return PatientReportFilterDTO.builder()
                .search(search)
                .gender(gender)
                .goal(goal)
                .activityLevel(activityLevel)
                .build();
    }
}
