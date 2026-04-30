package org.adch.multimodalparliamentexplorer.importer.dto.web;

import java.time.LocalDateTime;

public record ImporterResponse(
        LocalDateTime datetime,
        int totalSessionXmlsFound,
        int storedSessionsCount
) {
}
