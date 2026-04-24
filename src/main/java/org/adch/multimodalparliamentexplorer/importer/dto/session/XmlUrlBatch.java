package org.adch.multimodalparliamentexplorer.importer.dto.session;

import java.util.List;

public record XmlUrlBatch(List<String> xmlUrls) {

    public int batchSize() {return  xmlUrls.size();}
}
