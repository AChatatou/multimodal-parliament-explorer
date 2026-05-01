package org.adch.multimodalparliamentexplorer.pipeline.steps;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.adch.multimodalparliamentexplorer.importer.dto.MappedImportResult;
import org.adch.multimodalparliamentexplorer.member.MongoMemberRepository;
import org.adch.multimodalparliamentexplorer.pipeline.PipelineStep;
import org.adch.multimodalparliamentexplorer.session.MongoSessionRepository;
import org.adch.multimodalparliamentexplorer.session.Session;
import org.adch.multimodalparliamentexplorer.speech.MongoSpeechRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;


@AllArgsConstructor
@Slf4j
public class PersistenceStep implements PipelineStep<List<MappedImportResult>, Void> {

    private MongoSessionRepository sessionRepository;
    private MongoMemberRepository memberRepository;
    private MongoSpeechRepository speechRepository;


    @Override
    public CompletableFuture<Void> process(List<MappedImportResult> input) {

        return CompletableFuture.runAsync(() -> {

            memberRepository.saveAll(
                    input.stream()
                            .flatMap(r -> r.members().stream())
                            .toList()
            );

            speechRepository.saveAll(
                    input.stream()
                            .flatMap(r -> r.speeches().stream())
                            .toList()
            );

            var sessions = input.stream()
                    .map(MappedImportResult::session)
                    .toList();

            sessionRepository.saveAll(sessions);

            log.info("Saved data of sessions {}", sessions.stream().map(Session::getSessionNumber));
        });

    }
}
