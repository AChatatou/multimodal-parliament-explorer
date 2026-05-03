package org.adch.multimodalparliamentexplorer.nlp;

import lombok.AllArgsConstructor;
import org.adch.multimodalparliamentexplorer.importer.dto.web.ImporterResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/nlp")
@AllArgsConstructor
public class NLPController {

    private NLPService nlpService;


    @GetMapping("/import")
    public ResponseEntity<?> importData(@RequestParam String legislativePeriod){

        try {
            nlpService.importAllXmiData();

            return ResponseEntity
                    .accepted()
                    .body(true);

        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body("Import already running");
        }

    }


    @GetMapping("/status")
    public ResponseEntity<Boolean> importStatus(){
        return ResponseEntity.ok(nlpService.getRunningState());
    }


    @GetMapping("/{speechId}")
    public CompletableFuture<ResponseEntity<SpeechNlpData>> getNlpData(@PathVariable String speechId) {

        return nlpService.fetchSpeechNlpData(speechId, false)
                .thenApply(opt -> opt
                        .map(ResponseEntity::ok)
                        .orElseGet(() -> ResponseEntity.notFound().build())
                );
    }
}
