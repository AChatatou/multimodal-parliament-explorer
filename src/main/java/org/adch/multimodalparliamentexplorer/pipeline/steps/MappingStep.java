package org.adch.multimodalparliamentexplorer.pipeline.steps;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.adch.multimodalparliamentexplorer.importer.mapper.SpeechMapper;
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
public class MappingStep implements PipelineStep<List<SessionImportData>, List<MappedImportResult>> {

    private SessionMapper sessionMapper;
    private MemberMapper memberMapper;
    private SpeechMapper speechMapper;
    private MdbZipReader mdbZipReader;
    private MdbPhotoExtractor mdbPhotoExtractor;


    public MappedImportResult mappSessionData(SessionImportData sessionData) {

        var mappedMemberDataList = sessionData.speakersImportData()
                .stream()
                .map(speakerData -> {
                    var mdbZipData = mdbZipReader.extractMemberData(speakerData.speakerId());
                    var mdbPhoto = mdbPhotoExtractor
                            .getMemberPhoto(
                                speakerData.title(),
                                speakerData.firstName(),
                                speakerData.lastName());

                    return memberMapper.fromMdbZipData(mdbZipData, mdbPhoto);
                })
                .toList();

        var mappedSpeeches = sessionData.speechesImportData()
                .stream()
                .map(speechImportData ->
                        speechMapper.fromSpeechImportData(speechImportData))
                .toList();

        var mappedSession = sessionMapper.fromSessionImportData(sessionData, Instant.now());

        log.info("Mapped data of session {} successfully", sessionData.sessionMetadata().sessionNumber());
        return new MappedImportResult(mappedSession, mappedSpeeches, mappedMemberDataList);
    }



    @Override
    public CompletableFuture<List<MappedImportResult>> process(List<SessionImportData> input) {

            var futures = input.stream()
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

    }

}
