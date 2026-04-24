package org.adch.multimodalparliamentexplorer.importer.model.session;

import lombok.Builder;

@Builder
public record SpeakerData(
        String speakerId,
        String speakerFirstName,
        String speakerLastName,
        String faction
) {}
