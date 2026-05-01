package org.adch.multimodalparliamentexplorer.session;

import lombok.AllArgsConstructor;
import org.adch.multimodalparliamentexplorer.session.dto.SessionListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class SessionService {

    private MongoSessionRepository sessionRepository;

    public Page<SessionListDto> getAllSessions(Pageable pageable) {
        return sessionRepository.findAll(pageable)
                .map(this::toDto);
    }

    public Optional<Session> getSession(String sessionNumber) {
        return sessionRepository.findById(sessionNumber);
    }

    public List<SessionListDto> getSessionByLegislativePeriod(String legislativePeriod) {
        return sessionRepository.findByLegislativePeriod(legislativePeriod)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<SessionListDto> getSessionsBetweenDates(LocalDate start, LocalDate end) {
        return sessionRepository.findBySessionDateBetween(start, end)
                .stream()
                .map(this::toDto)
                .toList();
    }



    private SessionListDto toDto(Session session) {
        return new
                SessionListDto(
                        session.getSessionNumber(),
                        session.getSessionDate(),
                        session.getLegislativePeriod(),
                        session.getSpeeches()
                );
    }
}
