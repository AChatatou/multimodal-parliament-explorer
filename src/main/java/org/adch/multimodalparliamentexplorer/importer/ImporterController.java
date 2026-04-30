package org.adch.multimodalparliamentexplorer.importer;

import lombok.AllArgsConstructor;
import org.adch.multimodalparliamentexplorer.importer.dto.web.ImporterResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/importer")
@AllArgsConstructor
public class ImporterController {

    private final ImporterService importerService;

    @GetMapping
    public ResponseEntity<?> importData(@RequestParam String legislativePeriod){

        try {
            importerService.initImport(legislativePeriod);

            var response = new ImporterResponse(
                    true,
                    LocalDateTime.now(),
                    importerService.getTotalUrlsFound(),
                    importerService.getSavedSessionsCount());

            return ResponseEntity
                    .accepted()
                    .body(response);

        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body("Import already running");
        }

    }
}
