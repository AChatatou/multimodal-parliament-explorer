package org.adch.multimodalparliamentexplorer.member;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "mdb")
@Getter
@Builder
public class ParliamentMemberDoc {

    @Id
    private String id;

    private String firstName;
    private String lastName;
    private String occupation;
    private String faction;
    private String party;
    private ParliamentMemberPhoto photo;
    private String biography;

    private List<String> speeches;
}
