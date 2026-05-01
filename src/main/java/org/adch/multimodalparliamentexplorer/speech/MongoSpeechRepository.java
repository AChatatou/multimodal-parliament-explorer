package org.adch.multimodalparliamentexplorer.speech;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MongoSpeechRepository extends MongoRepository<Speech, String> {
    Page<Speech> findBySpeakerId(String speakerId , Pageable pageable);
    Page<Speech> findByLegislativePeriod(String legislativePeriod, Pageable pageable);

    Page<Speech> findByLegislativePeriodAndSpeakerId(String period, String speakerId, Pageable pageable);
}
