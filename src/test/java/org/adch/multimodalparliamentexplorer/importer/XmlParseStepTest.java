package org.adch.multimodalparliamentexplorer.importer;

import org.adch.multimodalparliamentexplorer.pipeline.steps.XmlParseStep;
import org.adch.multimodalparliamentexplorer.parser.XmlParser;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

public class XmlParseStepTest {

    private XmlParseStep xmlParseStep = new XmlParseStep(new XmlParser());


    @ParameterizedTest
    @ValueSource(strings = {"https://www.bundestag.de/resource/blob/1035090/20207.xml"})
    void shouldParseXml(String xmlUrl){

        var sessionData = xmlParseStep.safeExtractDataFromXml(xmlUrl);

        assertFalse(sessionData.isEmpty());
        assertNotNull(sessionData.get().sessionMetadata());
        assertTrue(sessionData.get().speechesImportData().size() > 1);

    }

}
