package org.adch.multimodalparliamentexplorer.nlp.entities;

import lombok.Builder;

import java.util.List;

@Builder
public record SentenceData(
    int begin,
    int end,
    double positivSentiment,
    double neutralSentiment,
    double negativSentiment,
    double totalSentiment,
    double sarcasm,
    double nonSarcasm,
    List<NamedEntityData> namedEntities,
    List<TopicData> topics
) {
}
