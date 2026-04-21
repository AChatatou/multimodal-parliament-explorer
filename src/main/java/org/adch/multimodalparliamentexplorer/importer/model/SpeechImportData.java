package org.adch.multimodalparliamentexplorer.importer.model;

import lombok.Builder;
import org.adch.multimodalparliamentexplorer.session.speech.Segment;


import java.util.List;

@Builder
public record SpeechImportData(
        String id,
        SpeakerData speakerData,
        List<Segment> segments
){
}
