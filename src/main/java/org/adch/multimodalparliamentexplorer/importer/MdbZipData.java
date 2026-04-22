package org.adch.multimodalparliamentexplorer.importer;

import lombok.Builder;

@Builder
public record MdbZipData(
        String id,
        String title,
        String firstName,
        String lastName,
        String party,
        String occupation,
        String biography
) {

}
