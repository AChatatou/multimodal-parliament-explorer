package org.adch.multimodalparliamentexplorer.importer.mapper;

import org.adch.multimodalparliamentexplorer.importer.dto.session.SpeechImportData;
import org.adch.multimodalparliamentexplorer.speech.Speech;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SpeechMapper {

    Speech fromSpeechImportData(SpeechImportData speechImportData);
}
