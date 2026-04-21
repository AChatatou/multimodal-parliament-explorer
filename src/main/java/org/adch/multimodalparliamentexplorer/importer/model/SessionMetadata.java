package org.adch.multimodalparliamentexplorer.importer.model;

import lombok.Builder;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
public record SessionMetadata(
        String sessionNumber,
        String legislativePeriod,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime
) {

    public static SessionMetadata emptyMetadata(){
        return new SessionMetadata(
                "?",
                "?",
                LocalDate.now(),
                LocalTime.now(),
                LocalTime.now());
    }
}
