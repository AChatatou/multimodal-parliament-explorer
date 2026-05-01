package org.adch.multimodalparliamentexplorer.session;

import lombok.AllArgsConstructor;
import org.adch.multimodalparliamentexplorer.session.dto.SessionListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/session")
@AllArgsConstructor
public class SessionController {

    private SessionService sessionService;

    @GetMapping
    public ResponseEntity<Page<SessionListDto>> getAllSessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
            ) {

            Pageable pageable = PageRequest.of(page, size, Sort.by("sessionNumber").ascending());

            return ResponseEntity.ok(sessionService.getAllSessions(pageable));
    }

    @GetMapping("/{period}")
    public ResponseEntity<List<SessionListDto>> getAllPeriodSessions(
            @PathVariable String period
    ) {

        return ResponseEntity.ok(sessionService.getSessionByLegislativePeriod(period));
    }

    @GetMapping("/{period}/{sessionNumber}")
    public ResponseEntity<Session> getSession(
            @PathVariable String period,
            @PathVariable String sessionNumber
    ) {

        var session = sessionService.getSession(period + sessionNumber);

        return session.map(ResponseEntity::ok).orElseGet(()-> ResponseEntity.notFound().build());
    }

}
