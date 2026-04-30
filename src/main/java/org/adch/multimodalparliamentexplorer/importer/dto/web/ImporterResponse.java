package org.adch.multimodalparliamentexplorer.importer.dto.web;

import java.time.LocalDateTime;

public record ImporterResponse(
        boolean importing,
        LocalDateTime datetime,
        int totalSessionXmlsFound,
        int storedSessionsCount
) {
}
