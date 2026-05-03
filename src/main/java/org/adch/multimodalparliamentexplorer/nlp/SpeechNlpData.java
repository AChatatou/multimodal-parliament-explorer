package org.adch.multimodalparliamentexplorer.nlp;


import lombok.Builder;
import lombok.Getter;
import org.adch.multimodalparliamentexplorer.nlp.entities.ParagraphData;
import org.adch.multimodalparliamentexplorer.nlp.entities.SentenceData;
import org.adch.multimodalparliamentexplorer.nlp.entities.TokenData;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "nlp")
@Getter
@Builder
public class SpeechNlpData {
    @Id
    private String id;

    private String fullText;

    private List<SentenceData> sentenceData;

    private List<TokenData> tokenData;

    private List<ParagraphData> paragraphData;


    public static SpeechNlpData ofDefault(String id){
        return SpeechNlpData.builder().id(id).build();
    }

}
