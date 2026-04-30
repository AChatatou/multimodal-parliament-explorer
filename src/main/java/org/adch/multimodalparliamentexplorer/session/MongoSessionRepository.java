package org.adch.multimodalparliamentexplorer.session;

import org.adch.multimodalparliamentexplorer.session.speech.Speech;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;

@Repository
public interface MongoSessionRepository extends MongoRepository<Session, String> {
    Set<Session> findByLegislativePeriod(String legislativePeriod);
}
