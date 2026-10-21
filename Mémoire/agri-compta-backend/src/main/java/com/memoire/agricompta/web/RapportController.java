package com.memoire.agricompta.web;

import com.memoire.agricompta.service.RapportService;
import com.memoire.agricompta.web.dto.RapportSyntheseResponse;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rapports")
@RequiredArgsConstructor
public class RapportController {
    private final RapportService rapportService;

    @GetMapping("/synthese")
    public RapportSyntheseResponse synthese(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(required = false) Long campagneId,
            @RequestParam(required = false) Long cultureId) {
        return rapportService.synthese(dateDebut, dateFin, campagneId, cultureId);
    }

    @GetMapping("/export.csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(required = false) Long campagneId,
            @RequestParam(required = false) Long cultureId) {
        byte[] csv = rapportService.exportCsv(dateDebut, dateFin, campagneId, cultureId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rapport-agri-compta.csv")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(csv);
    }
}
