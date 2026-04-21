package org.adch.multimodalparliamentexplorer.importer;


import org.adch.multimodalparliamentexplorer.parser.HtmlParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

public class IndexDiscoveryTest {

    private final IndexDiscovery indexDiscovery = new IndexDiscovery("https://www.bundestag.de/services/opendata", new HtmlParser());

    @AfterEach
    void reset() {
        indexDiscovery.reset();
    }

    @ParameterizedTest
    @ValueSource(strings = {"19", "20", "21"})
    void shouldExtractXmlUrlBatchFutures(String legislativePeriod){

        indexDiscovery.initDiscovery(legislativePeriod);

        assertNotNull(indexDiscovery.getSourceUrl());
        assertNotNull(indexDiscovery.getBaseUrl());
        assertTrue(indexDiscovery.getTotalXmlUrlCount() > 0);
        assertFalse(indexDiscovery.getBatchesFutures().isEmpty());
    }


    @Test
    void shouldGetBatchAndIncrement(){
        indexDiscovery.initDiscovery("20");
        assertTrue(indexDiscovery.getNextUrlBatch().join().batchSize() > 0);
        assertEquals(1, indexDiscovery.getFetchedBatchesCount());
    }
}
