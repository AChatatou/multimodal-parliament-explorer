package org.adch.multimodalparliamentexplorer.importer.dto.mdb;

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

    public static MdbZipData emptyMdbData(){
        return new MdbZipData("?",
                "?",
                "?",
                "?",
                "?",
                "?",
                "");
    }
}
