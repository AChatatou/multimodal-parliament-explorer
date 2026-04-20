package org.adch.multimodalparliamentexplorer.nlp;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

@Document(collection = "nlp")
public class NlpData {

    @Id
    private String id;

}
