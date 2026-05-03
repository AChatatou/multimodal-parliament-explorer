package org.adch.multimodalparliamentexplorer.nlp;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.adch.multimodalparliamentexplorer.nlp.mapper.JcasMapper;
import org.adch.multimodalparliamentexplorer.speech.Speech;
import org.apache.uima.jcas.JCas;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class NLPService {

    private SpeechNlpDataRepository repository;
    private NLPAnalyser analyser;
    private NLPDataImporter importer;


    public CompletableFuture<SpeechNlpData> fetchSpeechNlpData(Speech speech, boolean runIfNotFound) {

        return CompletableFuture.supplyAsync(() -> repository.findById(speech.getId()))
                .thenCompose(optional -> {
                    if (optional.isPresent()) {
                        return CompletableFuture.completedFuture(optional.get());
                    }

                    log.info("NLP data for speech {} not found", speech.getId());

                    if (!runIfNotFound) {
                        return CompletableFuture.completedFuture(
                                SpeechNlpData.ofDefault(speech.getId(), speech.getFullText())
                        );
                    }

                    return CompletableFuture.supplyAsync(() -> {
                        var jCas = analyse(speech);
                        var data = JcasMapper.mapNlpData(jCas);

                        return repository.save(data);
                    });
                });
    }


    public JCas analyse(Speech speech) {
        try {
            return analyser.analyse(speech, "de");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public List<SpeechNlpData> importAllXmiData() {

        var jCasList = importer.importAllXmis();

        return jCasList.stream()
                .map(this::saveNlpData)
                .toList();
    }

    public SpeechNlpData saveNlpData(SpeechNlpData speechNlpData) {

        if (repository.existsById(speechNlpData.getId())) {
            log.info("Speech {} NLP data already exists in the database. Skipping save", speechNlpData.getId());
            return speechNlpData;
        }

        log.info("Saving NLP data for Speech {} to database...", speechNlpData.getId());
        return repository.save(speechNlpData);
    }

    public SpeechNlpData saveNlpData(JCas jCas) {

       var nlpData = JcasMapper.mapNlpData(jCas);
        return saveNlpData(nlpData);
    }

    public List<SpeechNlpData> saveAllNlpData(List<JCas> jCasList) {

        List<SpeechNlpData> mappedData = jCasList.stream()
                .map(JcasMapper::mapNlpData)
                .toList();

        if (mappedData.isEmpty()) {
            log.info("No NLP data provided");
            return List.of();
        }

        // Fetch existing IDs
        Set<String> existingIds = repository.findAllById(
                        mappedData.stream()
                                .map(SpeechNlpData::getId)
                                .toList()
                ).stream()
                .map(SpeechNlpData::getId)
                .collect(Collectors.toSet());

        // Filter new data
        List<SpeechNlpData> newData = mappedData.stream()
                .filter(data -> !existingIds.contains(data.getId()))
                .toList();

        if (newData.isEmpty()) {
            log.info("No new NLP data to save");
            return List.of();
        }

        log.info("Saving {} new NLP data records", newData.size());
        return repository.saveAll(newData);
    }


}
