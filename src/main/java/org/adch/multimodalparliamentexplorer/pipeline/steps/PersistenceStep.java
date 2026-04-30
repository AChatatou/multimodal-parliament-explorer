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
public class PersistenceStep implements PipelineStep<List<MappedImportResult>, Void> {

    private MongoSessionRepository sessionRepository;
    private MongoMemberRepository memberRepository;


    @Override
    public CompletableFuture<Void> process(List<MappedImportResult> input) {

        return CompletableFuture.runAsync(() -> {

            memberRepository.saveAll(
                    input.stream()
                            .flatMap(r -> r.members().stream())
                            .toList()
            );

            sessionRepository.saveAll(
                    input.stream()
                            .map(MappedImportResult::session)
                            .toList()
            );

        });

    }
}
