package org.adch.multimodalparliamentexplorer.pipeline.steps;

import lombok.AllArgsConstructor;
import org.adch.multimodalparliamentexplorer.importer.MdbPhotoExtractor;
import org.adch.multimodalparliamentexplorer.importer.MdbZipReader;
import org.adch.multimodalparliamentexplorer.importer.dto.MappedImportResult;
import org.adch.multimodalparliamentexplorer.importer.dto.session.SessionImportData;
import org.adch.multimodalparliamentexplorer.importer.mapper.MemberMapper;
import org.adch.multimodalparliamentexplorer.importer.mapper.SessionMapper;
import org.adch.multimodalparliamentexplorer.pipeline.PipelineStep;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
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
                            .thenApplyAsync(v->
                                    futures.stream()
                                            .map(CompletableFuture::join)
                                            .toList()
                            );
                });
    }

}
