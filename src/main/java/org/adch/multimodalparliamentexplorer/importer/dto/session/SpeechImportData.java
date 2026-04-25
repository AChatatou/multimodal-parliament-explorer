package org.adch.multimodalparliamentexplorer.importer.dto.session;

import lombok.Builder;
import org.adch.multimodalparliamentexplorer.session.speech.Segment;


import java.util.List;

@Builder
public record SpeechImportData(
        String id,
        String speakerId,
        String faction,
        List<Segment> segments
){
}
