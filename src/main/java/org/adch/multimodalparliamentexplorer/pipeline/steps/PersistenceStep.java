package org.adch.multimodalparliamentexplorer.pipeline.steps;

import lombok.AllArgsConstructor;
import org.adch.multimodalparliamentexplorer.importer.dto.MappedImportResult;
import org.adch.multimodalparliamentexplorer.member.MongoMemberRepository;
import org.adch.multimodalparliamentexplorer.pipeline.PipelineStep;
import org.adch.multimodalparliamentexplorer.session.MongoSessionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;


@AllArgsConstructor
public class PersistenceStep implements PipelineStep<CompletableFuture<List<MappedImportResult>>, CompletableFuture<Void>> {

    private MongoSessionRepository sessionRepository;
    private MongoMemberRepository memberRepository;


    @Override
    public CompletableFuture<Void> process(CompletableFuture<List<MappedImportResult>> input) {

        return input.thenAccept(results -> {
            memberRepository.saveAll(
                    results.stream()
                            .flatMap(r -> r.members().stream())
                            .toList()
            );
            sessionRepository.saveAll(
                    results.stream()
                            .map(MappedImportResult::session)
                            .toList()
            );

        });
    }
}
