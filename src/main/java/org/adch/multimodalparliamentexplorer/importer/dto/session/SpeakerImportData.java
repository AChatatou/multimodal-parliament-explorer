package org.adch.multimodalparliamentexplorer.importer.dto.session;

import lombok.Builder;

@Builder
public record SpeakerImportData(
        String speakerId,
        String firstName,
        String lastName,
        String faction
) {}
