package org.adch.multimodalparliamentexplorer.speech;


import lombok.AllArgsConstructor;
import org.adch.multimodalparliamentexplorer.session.dto.SessionListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/speech")
@AllArgsConstructor
public class SpeechController {

    private SpeechService speechService;

    @GetMapping
    public ResponseEntity<Page<Speech>> getAllSpeeches(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String speakerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("id").ascending()
        );

        return ResponseEntity.ok(
                speechService.getSpeeches(period, speakerId, pageable)
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<Speech> getSpeech(
            @PathVariable String id
    ) {

        var speech = speechService.getSpeech(id);

        return speech.map(ResponseEntity::ok).orElseGet(()-> ResponseEntity.notFound().build());
    }


}
