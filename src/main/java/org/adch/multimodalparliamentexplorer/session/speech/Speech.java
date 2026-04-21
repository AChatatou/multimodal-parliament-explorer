package org.adch.multimodalparliamentexplorer.session.speech;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Getter
@Builder
public class Speech {

    private String id;

    private String sessionNumber;

    private String speakerId;

    private String Faction;

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
