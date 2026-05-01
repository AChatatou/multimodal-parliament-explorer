package org.adch.multimodalparliamentexplorer.importer;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.adch.multimodalparliamentexplorer.importer.mapper.MemberMapper;
import org.adch.multimodalparliamentexplorer.importer.mapper.SessionMapper;
import org.adch.multimodalparliamentexplorer.importer.mapper.SpeechMapper;
import org.adch.multimodalparliamentexplorer.importer.tools.MdbPhotoExtractor;
import org.adch.multimodalparliamentexplorer.importer.tools.MdbZipReader;
import org.adch.multimodalparliamentexplorer.importer.tools.XmlIndexDiscovery;
import org.adch.multimodalparliamentexplorer.member.MongoMemberRepository;
import org.adch.multimodalparliamentexplorer.parser.XmlParser;
import org.adch.multimodalparliamentexplorer.pipeline.AsyncPipeline;
import org.adch.multimodalparliamentexplorer.pipeline.steps.MappingStep;
import org.adch.multimodalparliamentexplorer.pipeline.steps.PersistenceStep;
import org.adch.multimodalparliamentexplorer.pipeline.steps.XmlParseStep;
import org.adch.multimodalparliamentexplorer.session.MongoSessionRepository;
import org.adch.multimodalparliamentexplorer.session.Session;
import org.adch.multimodalparliamentexplorer.speech.MongoSpeechRepository;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class ImporterService {

    private XmlIndexDiscovery xmlIndexDiscovery;
    private MdbZipReader mdbZipReader;
    private MdbPhotoExtractor mdbPhotoExtractor;
    private XmlParser xmlParser;

    private MemberMapper memberMapper;
    private SessionMapper sessionMapper;
    private SpeechMapper speechMapper;

    private MongoMemberRepository memberRepository;
    private MongoSessionRepository sessionRepository;
    private MongoSpeechRepository speechRepository;

    private final AtomicBoolean running = new AtomicBoolean(false);


    public CompletableFuture<Void> initImport(String legislativePeriod){

        if (!running.compareAndSet(false, true)) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("Import already running")
            );
        }

        var savedSessions = getSavedSessionXmlUrls(legislativePeriod);

        xmlIndexDiscovery.initDiscovery(legislativePeriod, savedSessions);

        if(savedSessions.size() == xmlIndexDiscovery.getTotalXmlUrlCount()){
            log.info("Skipping import...");
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<Void> chain = CompletableFuture.completedFuture(null);

        while (xmlIndexDiscovery.hasNext()) {

            chain = chain.thenCompose(v ->
                    xmlIndexDiscovery.getNextUrlBatch()
                            .thenCompose(batch ->
                                    AsyncPipeline.of(new XmlParseStep(xmlParser))
                                            .then(new MappingStep(sessionMapper, memberMapper, speechMapper, mdbZipReader, mdbPhotoExtractor))
                                            .then(new PersistenceStep(sessionRepository, memberRepository, speechRepository))
                                            .execute(batch)
                            )
            );
        }

        return chain.whenComplete((res, ex) -> {
            running.set(false);
        });
    }

    public Set<String> getSavedSessionXmlUrls(String legislativePeriod){
        return sessionRepository
                .findByLegislativePeriod(legislativePeriod)
                .stream()
                .map(Session::getSourceXmlUrl)
                .collect(Collectors.toSet());
    }


    public int getTotalUrlsFound() {
        return xmlIndexDiscovery.getTotalXmlUrlCount();
    }

    public int getFetchedUrlsCount(){
        return xmlIndexDiscovery.getUrlsFetched();
    }

    public int getSavedSessionsCount() {return (int) sessionRepository.count();}

    public boolean getRunningState(){return running.get();}

}
