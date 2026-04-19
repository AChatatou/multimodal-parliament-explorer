package org.adch.multimodalparliamentexplorer.session;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoSessionRepository extends MongoRepository<Session, String> {
}
