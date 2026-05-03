package org.adch.multimodalparliamentexplorer.nlp.entities;

public record NamedEntityData(
        int begin,
        int end,
        String value
) {
}
