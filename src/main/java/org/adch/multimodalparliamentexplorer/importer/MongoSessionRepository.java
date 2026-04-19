package org.adch.multimodalparliamentexplorer.importer;

import org.adch.multimodalparliamentexplorer.importer.model.Session;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoSessionRepository extends MongoRepository<Session, String> {
}
