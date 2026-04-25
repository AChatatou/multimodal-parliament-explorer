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

    @Mapping(target = "sourceXmlUrl", source = "sessionImportData.xmlUrl")
    @Mapping(target = "sessionNumber", source = "sessionImportData.sessionMetadata.sessionNumber")
    @Mapping(target = "legislativePeriod", source = "sessionImportData.sessionMetadata.legislativePeriod")
    @Mapping(target = "sessionDate", source = "sessionImportData.sessionMetadata.date")
    @Mapping(target = "startTime", source = "sessionImportData.sessionMetadata.startTime")
    @Mapping(target = "endTime", source = "sessionImportData.sessionMetadata.endTime")
    @Mapping(target = "speeches", source = "sessionImportData.speechesImportData")
    @Mapping(target = "importDate", source = "importDate")
    Session fromSessionImportData(SessionImportData sessionImportData, Instant importDate);
}
