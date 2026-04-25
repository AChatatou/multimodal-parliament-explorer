package org.adch.multimodalparliamentexplorer.importer.mapper;

import org.adch.multimodalparliamentexplorer.importer.dto.session.SessionImportData;
import org.adch.multimodalparliamentexplorer.importer.dto.session.SpeechImportData;
import org.adch.multimodalparliamentexplorer.session.Session;
import org.adch.multimodalparliamentexplorer.session.speech.Speech;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;

@Mapper(componentModel = "spring")
public interface SessionMapper {


    Speech fromSpeechImportData(SpeechImportData speechImportData);

    @Mapping(target = "sourceXmlUrl", source = "xmlUrl")
    @Mapping(target = "sessionNumber", source = "sessionMetadata.sessionNumber")
    @Mapping(target = "legislativePeriod", source = "sessionMetadata.legislativePeriod")
    @Mapping(target = "sessionDate", source = "sessionMetadata.date")
    @Mapping(target = "startTime", source = "sessionMetadata.startTime")
    @Mapping(target = "endTime", source = "sessionMetadata.endTime")
    @Mapping(target = "speeches", source = "speechesImportData")
    @Mapping(target = "importDate", source = "importDate")
    Session fromSessionImportData(SessionImportData data, Instant importDate);
}
