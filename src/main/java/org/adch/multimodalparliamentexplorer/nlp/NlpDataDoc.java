package org.adch.multimodalparliamentexplorer.nlp;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "nlp")
public class NlpDataDoc {

    @Id
    private String id;

}
