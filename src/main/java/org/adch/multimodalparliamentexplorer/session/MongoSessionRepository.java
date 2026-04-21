package org.adch.multimodalparliamentexplorer.session;

import org.adch.multimodalparliamentexplorer.session.speech.Speech;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoSessionRepository extends MongoRepository<Speech, String> {
}
