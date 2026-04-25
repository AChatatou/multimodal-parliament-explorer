package org.adch.multimodalparliamentexplorer.importer.dto.session;

import lombok.Builder;

@Builder
public record SpeakerImportData(
        String speakerId,
        String title,
        String firstName,
        String lastName,
        String faction
) {}
