package org.adch.multimodalparliamentexplorer.speech;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class SpeechService {

    private MongoSpeechRepository speechRepository;

    public Optional<Speech> getSpeech(String id) {
        return speechRepository.findById(id);
    }

    public Page<Speech> getAllSpeeches(Pageable pageable) {

        return speechRepository.findAll(pageable);
    }

    public Page<Speech> getAllSpeeches(String legislativePeriod, Pageable pageable) {

        return speechRepository.findByLegislativePeriod(legislativePeriod, pageable);
    }

    public List<Speech> getAllMemberSpeeches(String speakerId) {
        return speechRepository.findBySpeakerId(speakerId);
    }
}
