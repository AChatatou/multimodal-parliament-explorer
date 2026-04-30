package org.adch.multimodalparliamentexplorer.importer;

import org.adch.multimodalparliamentexplorer.importer.tools.MdbZipReader;
import org.adch.multimodalparliamentexplorer.parser.XmlParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MdbZipReaderTest {

    private final MdbZipReader mdbZipReader = new MdbZipReader("https://www.bundestag.de/resource/blob/472878/MdB-Stammdaten.zip",
            new XmlParser());


    @Test
    void shouldExtractMdb() throws Exception {

        var mdbData = mdbZipReader.extractMemberData("11005000");
        assertEquals("11005000", mdbData.id());
        assertEquals("Abdi", mdbData.lastName());
        assertEquals("Sanae", mdbData.firstName());
        assertEquals("", mdbData.title());
        assertEquals("SPD", mdbData.party());
        assertEquals("Vertragsmanagerin, Finanzmanagerin", mdbData.occupation());

    }
}
