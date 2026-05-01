package org.adch.multimodalparliamentexplorer.speech;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MongoSpeechRepository extends MongoRepository<Speech, String> {
    List<Speech> findBySpeakerId(String speakerId);
    Page<Speech> findByLegislativePeriod(String legislativePeriod, Pageable pageable);
}
