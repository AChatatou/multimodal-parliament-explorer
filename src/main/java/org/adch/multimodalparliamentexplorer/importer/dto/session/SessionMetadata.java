package org.adch.multimodalparliamentexplorer.importer.dto.session;

import lombok.Builder;

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
