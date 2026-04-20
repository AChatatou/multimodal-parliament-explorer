package org.adch.multimodalparliamentexplorer.importer;


import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

public class IndexDiscoveryTest {

    private final IndexDiscovery indexDiscovery = new IndexDiscovery("https://www.bundestag.de/services/opendata");


    @ParameterizedTest
    @ValueSource(strings = {"19", "20", "21"})
    void shouldExtractXmlUrlBatches(String legislativePeriod){

        indexDiscovery.initDiscovery(legislativePeriod);

        assertNotNull(indexDiscovery.getSourceUrl());
        assertNotNull(indexDiscovery.getBaseUrl());
        assertTrue(indexDiscovery.getTotalXmlUrlCount() > 0);
        assertFalse(indexDiscovery.getBatchesFutures().isEmpty());
    }
}
