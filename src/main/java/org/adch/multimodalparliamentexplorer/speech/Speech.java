package org.adch.multimodalparliamentexplorer.speech;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
@Builder
@Document(collection = "speech")
public class Speech {

    @Id
    private String id;

    private String sessionNumber;

    private String legislativePeriod;

    private String speakerId;

    private String faction;

    private List<Segment> segments ;

    private String getText(Predicate<Segment> predicate) {

        return Optional.ofNullable(segments)
                .orElseGet(List::of)
                .stream()
                .filter(predicate)
                .map(Segment::text)
                .collect(Collectors.joining(" "));
    }

    public String getSpeechText() {
        return getText(segment -> ! segment.isComment());
    }

    public String getFullText() {
        return getText(segment -> true);
    }




}
