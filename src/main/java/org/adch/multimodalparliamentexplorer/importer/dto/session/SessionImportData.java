package org.adch.multimodalparliamentexplorer.importer.dto.session;

import lombok.Builder;

import java.util.List;

@Builder
public record SessionImportData(
        String xmlUrl,
        SessionMetadata sessionMetadata,
        List<SpeechImportData> speechesImportData
) {
}
