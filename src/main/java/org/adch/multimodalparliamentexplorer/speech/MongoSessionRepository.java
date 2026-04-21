package org.adch.multimodalparliamentexplorer.speech;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoSessionRepository extends MongoRepository<Speech, String> {
}
