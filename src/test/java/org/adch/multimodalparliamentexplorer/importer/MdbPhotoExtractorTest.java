package org.adch.multimodalparliamentexplorer.importer;

import org.adch.multimodalparliamentexplorer.importer.tools.MdbPhotoExtractor;
import org.adch.multimodalparliamentexplorer.parser.HtmlParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MdbPhotoExtractorTest {

    private final MdbPhotoExtractor mdbPhotoExtractor = new MdbPhotoExtractor("https://www.bundestag.de/ajax/filterlist/webarchiv/abgeordnete/biografien20/862712-862712",
            new HtmlParser());

    @Test
    void shouldFetchPhoto() {

        var photo = mdbPhotoExtractor.getMemberPhoto("", "Sanae", "Abdi");
        assertEquals("Abdi, Sanae", photo.name());
        assertNotNull(photo.url());
    }
}
