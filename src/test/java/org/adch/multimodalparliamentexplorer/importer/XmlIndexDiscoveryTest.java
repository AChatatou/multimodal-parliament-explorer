package org.adch.multimodalparliamentexplorer.importer;


import org.adch.multimodalparliamentexplorer.importer.tools.XmlIndexDiscovery;
import org.adch.multimodalparliamentexplorer.parser.HtmlParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class XmlIndexDiscoveryTest {

    private final XmlIndexDiscovery xmlIndexDiscovery = new XmlIndexDiscovery("https://www.bundestag.de/services/opendata", new HtmlParser());

    @AfterEach
    void reset() {
        xmlIndexDiscovery.reset();
    }

    @ParameterizedTest
    @ValueSource(strings = {"19", "20", "21"})
    void shouldExtractXmlUrlBatchFutures(String legislativePeriod){

        xmlIndexDiscovery.initDiscovery(legislativePeriod, Set.of());

        assertNotNull(xmlIndexDiscovery.getSourceUrl());
        assertNotNull(xmlIndexDiscovery.getBaseUrl());
        assertTrue(xmlIndexDiscovery.getTotalXmlUrlCount() > 0);
        assertFalse(xmlIndexDiscovery.getBatchesFutures().isEmpty());
    }


    @Test
    void shouldGetBatchAndIncrement(){
        xmlIndexDiscovery.initDiscovery("20", Set.of());
        assertEquals(10, xmlIndexDiscovery.getNextUrlBatch().join().batchSize());
        assertEquals(1, xmlIndexDiscovery.getFetchedBatchesCount());
    }

    @Test
    void shouldSkipUrl(){
        xmlIndexDiscovery.initDiscovery("20", Set.of("https://www.bundestag.de/resource/blob/1057624/20214.xml", "https://www.bundestag.de/resource/blob/1033526/20205.xml"));
        assertEquals(8, xmlIndexDiscovery.getNextUrlBatch().join().batchSize());
    }
}
