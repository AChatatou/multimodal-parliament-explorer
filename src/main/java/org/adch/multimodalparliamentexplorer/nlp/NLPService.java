package org.adch.multimodalparliamentexplorer.nlp;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.adch.multimodalparliamentexplorer.nlp.mapper.JcasMapper;
import org.adch.multimodalparliamentexplorer.speech.MongoSpeechRepository;
import org.adch.multimodalparliamentexplorer.speech.Speech;
import org.apache.uima.jcas.JCas;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class NLPService {

    private SpeechNlpDataRepository nlpRepository;
    private MongoSpeechRepository speechRepository;
    private NLPAnalyser analyser;
    private NLPDataImporter importer;
    private final AtomicBoolean importRunning = new AtomicBoolean(false);


    public CompletableFuture<Optional<SpeechNlpData>> fetchSpeechNlpData(String speechId, boolean runIfNotFound) {

        return CompletableFuture
                .supplyAsync(() ->
                    nlpRepository.findById(speechId))
                .thenCompose(optional -> {

                    if (optional.isPresent())
                        return CompletableFuture.completedFuture(optional);

                    if (!runIfNotFound) {
                        log.warn("NLP data for speech with id {} not found ", speechId);
                        return CompletableFuture.completedFuture(Optional.empty());
                    }

                    if (!speechRepository.existsById(speechId)) {
                        log.warn("Speech with id {} does not exist", speechId);
                        return CompletableFuture.completedFuture(Optional.empty());
                    }

                    return CompletableFuture
                            .supplyAsync(() ->
                                    speechRepository.findById(speechId)
                            )
                            .thenCompose(speechOpt -> speechOpt
                                            .map(speech ->
                                                    CompletableFuture
                                                            .supplyAsync(() -> analyse(speech))
                                                            .thenApply(JcasMapper::mapNlpData)
                                                            .thenApply(Optional::of)
                                            )
                                            .orElseGet(() ->
                                                    CompletableFuture.completedFuture(Optional.empty())
                                            )
                            );
                });
    }


    public JCas analyse(Speech speech) {
        try {
            return analyser.analyse(speech, "de");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public CompletableFuture<Void> importAllXmiData() {

        if (!importRunning.compareAndSet(false, true)) {
            log.warn("NLP import is already running");
                return CompletableFuture.failedFuture(
                        new IllegalStateException("Import already running")
                );
        }


        var futures = importer.importAllXmis();

        CompletableFuture<Void> all =
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .thenCompose(v -> {
                            var jCasList = futures.stream()
                                    .map(CompletableFuture::join)
                                    .filter(Objects::nonNull)
                                    .toList();

                            return CompletableFuture.runAsync(() -> saveAllNlpData(jCasList));
                        });


        return all.whenComplete((res, ex) -> importRunning.set(false));

    }

    public SpeechNlpData saveNlpData(SpeechNlpData speechNlpData) {

        if (nlpRepository.existsById(speechNlpData.getId())) {
            log.info("Speech {} NLP data already exists in the database. Skipping save", speechNlpData.getId());
            return speechNlpData;
        }

        log.info("Saving NLP data for Speech {} to database...", speechNlpData.getId());
        return nlpRepository.save(speechNlpData);
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
        Set<String> existingIds = nlpRepository.findAllById(
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
        return nlpRepository.saveAll(newData);
    }


    public Boolean getRunningState() {
        return importRunning.get();
    }
}
