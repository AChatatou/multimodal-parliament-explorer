package org.adch.multimodalparliamentexplorer.importer.dto.mdb;

import lombok.Builder;

@Builder
public record MdbPhoto(
        String id,
        String name,
        String url,
        String altText,
        String copyright
) {

}
