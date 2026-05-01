package org.adch.multimodalparliamentexplorer.session.dto;

import java.time.LocalDate;
import java.util.List;

public record SessionListDto(
        String sessionNumber,
        LocalDate date,
        String legislativePeriod,
        List<String> speeches

) {
}
