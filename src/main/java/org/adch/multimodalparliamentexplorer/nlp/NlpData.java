package org.adch.multimodalparliamentexplorer.nlp;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "nlp")
public class NlpData {

    @Id
    private String id;

}
