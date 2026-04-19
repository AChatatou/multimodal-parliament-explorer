package org.adch.multimodalparliamentexplorer.speech;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoSpeechRepository extends MongoRepository<SpeechDoc, String> {
}
