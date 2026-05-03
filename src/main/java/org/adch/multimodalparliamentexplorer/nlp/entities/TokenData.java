package org.adch.multimodalparliamentexplorer.nlp.entities;

import lombok.Builder;

@Builder
public record TokenData(
    int begin,
    int end,
    String stem,
    String lemma,
    String pos,
    String coarseValue,
    String gender,
    String number,
    String casus,
    String tense,
    String verbForm,
    String pronoun
) {
}
