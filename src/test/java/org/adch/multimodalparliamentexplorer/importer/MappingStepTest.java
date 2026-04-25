package org.adch.multimodalparliamentexplorer.importer;

import org.adch.multimodalparliamentexplorer.importer.dto.session.SessionImportData;
import org.adch.multimodalparliamentexplorer.importer.dto.session.SessionMetadata;
import org.adch.multimodalparliamentexplorer.importer.dto.session.SpeakerImportData;
import org.adch.multimodalparliamentexplorer.importer.dto.session.SpeechImportData;
import org.adch.multimodalparliamentexplorer.importer.mapper.MemberMapper;
import org.adch.multimodalparliamentexplorer.importer.mapper.SessionMapper;
import org.adch.multimodalparliamentexplorer.importer.steps.MappingStep;
import org.adch.multimodalparliamentexplorer.parser.HtmlParser;
import org.adch.multimodalparliamentexplorer.parser.XmlParser;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MappingStepTest {


    private final XmlParser xmlParser = new XmlParser();
    private final SessionMapper sessionMapper = Mappers.getMapper(SessionMapper.class);
    private final MemberMapper memberMapper = Mappers.getMapper(MemberMapper.class);
    private final MdbZipReader mdbZipReader = new MdbZipReader("https://www.bundestag.de/resource/blob/472878/MdB-Stammdaten.zip", xmlParser);
    private final MdbPhotoExtractor mdbPhotoExtractor = new MdbPhotoExtractor("https://www.bundestag.de/ajax/filterlist/webarchiv/abgeordnete/biografien20/862712-862712", new HtmlParser());
    private final MappingStep mappingStep = new MappingStep(sessionMapper, memberMapper, mdbZipReader, mdbPhotoExtractor);


    @Test
    void shouldMapSessionData(){

        var sessionMetadata = SessionMetadata.builder()
                .sessionNumber("sessionNumber")
                .date(LocalDate.now())
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(9, 0))
                .legislativePeriod("legislativePeriod")
                .build();

        var speechData = List.of(
                SpeechImportData.builder()
                        .id("speechId")
                        .speakerId("speakerId")
                        .faction("faction")
                        .build());

        var speakerData = List.of(
                SpeakerImportData.builder()
                        .speakerId("11005000")
                        .title("")
                        .firstName("Sanae")
                        .lastName("Abdi")
                        .faction("SPD")
                        .build());

        var sessionData = new SessionImportData("xmlUrl", sessionMetadata, speechData, speakerData);

        var result = mappingStep.mappSessionData(sessionData);

        assertNotNull(result);

        assertEquals("sessionNumber", result.session().getSessionNumber());
        assertEquals("legislativePeriod", result.session().getLegislativePeriod());
        assertEquals(LocalTime.of(8, 0), result.session().getStartTime());

        assertEquals(1, result.session().getSpeeches().size());
        assertEquals("speechId", result.session().getSpeeches().getFirst().getId());

        assertEquals(1, result.members().size());
        assertEquals("11005000", result.members().getFirst().getId());

    }
}
