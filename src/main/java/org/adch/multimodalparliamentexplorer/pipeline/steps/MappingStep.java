package org.adch.multimodalparliamentexplorer.pipeline.steps;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.adch.multimodalparliamentexplorer.importer.tools.MdbPhotoExtractor;
import org.adch.multimodalparliamentexplorer.importer.tools.MdbZipReader;
import org.adch.multimodalparliamentexplorer.importer.dto.MappedImportResult;
import org.adch.multimodalparliamentexplorer.importer.dto.session.SessionImportData;
import org.adch.multimodalparliamentexplorer.importer.mapper.MemberMapper;
import org.adch.multimodalparliamentexplorer.importer.mapper.SessionMapper;
import org.adch.multimodalparliamentexplorer.pipeline.PipelineStep;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@AllArgsConstructor
public class MappingStep implements PipelineStep<CompletableFuture<List<SessionImportData>>, CompletableFuture<List<MappedImportResult>>> {

    private SessionMapper sessionMapper;
    private MemberMapper memberMapper;
    private MdbZipReader mdbZipReader;
    private MdbPhotoExtractor mdbPhotoExtractor;


    public MappedImportResult mappSessionData(SessionImportData sessionData) {

        var mappedSession = sessionMapper.fromSessionImportData(sessionData, Instant.now());
        var mappedMemberDataList = sessionData.speakersImportData()
                .stream()
                .map(speakerData -> {
                    var mdbZipData = mdbZipReader.extractMemberData(speakerData.speakerId());
                    var mdbPhoto = mdbPhotoExtractor.getMemberPhoto(speakerData.title(),
                            speakerData.firstName(),
                            speakerData.lastName());
                    return memberMapper.fromMdbZipData(mdbZipData, mdbPhoto);
                })
                .toList();

        log.info("Mapped data from session {} successfully", sessionData.sessionMetadata().sessionNumber());
        return new MappedImportResult(mappedSession, mappedMemberDataList);
    }



    @Override
    public CompletableFuture<List<MappedImportResult>> process(CompletableFuture<List<SessionImportData>> input) {
        return input
                .thenCompose(importDataList -> {
                    var futures = importDataList.stream()
                            .map(sessionData ->
                                    CompletableFuture.supplyAsync(() -> mappSessionData(sessionData)))
                            .toList();

                    return CompletableFuture
                            .allOf(futures.toArray(new CompletableFuture[0]))
                            .thenApply(v->
                                    futures.stream()
                                            .map(CompletableFuture::join)
                                            .toList()
                            );
                });
    }

}
