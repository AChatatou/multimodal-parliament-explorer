package org.adch.multimodalparliamentexplorer.session;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Document(collection = "session")
@Getter
@Builder
public class Session {

    @Id
    private String sessionNumber;

    private String legislativePeriod;

    private LocalDate sessionDate;

    private LocalTime startTime;
    private LocalTime endTime;

    private String sourceXmlUrl;

    private Instant importDate;

    private List<String> speeches;

    public int getNumberOfSpeeches() {
        return speeches.size();
    }
}
