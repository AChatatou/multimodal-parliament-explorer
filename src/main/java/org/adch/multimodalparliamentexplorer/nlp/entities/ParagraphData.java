package org.adch.multimodalparliamentexplorer.nlp.entities;

import lombok.Builder;

@Builder
public record ParagraphData(
        int begin,
        int end,
        boolean comment
) {
}
