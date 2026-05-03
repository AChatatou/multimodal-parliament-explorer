package org.adch.multimodalparliamentexplorer.nlp;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface SpeechNlpDataRepository extends MongoRepository<SpeechNlpData, String> {
}
