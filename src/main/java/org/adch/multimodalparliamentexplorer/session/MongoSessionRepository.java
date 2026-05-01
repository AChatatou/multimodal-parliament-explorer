package org.adch.multimodalparliamentexplorer.session;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Repository
public interface MongoSessionRepository extends MongoRepository<Session, String> {
    Set<Session> findByLegislativePeriod(String legislativePeriod);

    List<Session> findBySessionDateBetween(LocalDate start, LocalDate end);
}
