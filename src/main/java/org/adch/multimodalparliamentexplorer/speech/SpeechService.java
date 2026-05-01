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

    public Page<Speech> getSpeeches(String period, String speakerId, Pageable pageable) {

        if (period != null && speakerId != null) {
            return speechRepository.findByLegislativePeriodAndSpeakerId(period, speakerId, pageable);
        }

        if (period != null) {
            return speechRepository.findByLegislativePeriod(period, pageable);
        }

        if (speakerId != null) {
            return speechRepository.findBySpeakerId(speakerId, pageable);
        }

        return speechRepository.findAll(pageable);
    }

}
