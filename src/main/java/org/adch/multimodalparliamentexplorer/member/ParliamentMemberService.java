package org.adch.multimodalparliamentexplorer.member;

import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class ParliamentMemberService {

    private MongoMemberRepository memberRepository;
    private final MongoTemplate mongoTemplate;


    public List<ParliamentMember> getAllMembers(){
        return memberRepository.findAll();
    }

    public Optional<ParliamentMember> getMember(String id){
        return memberRepository.findById(id);
    }

    public List<ParliamentMember> getPartyMembers(String party) {
        return memberRepository.findByParty(party);
    }

    public List<ParliamentMember> getFactionMembers(String faction) {
        return memberRepository.findByFaction(faction);
    }

    public List<ParliamentMember> getMembers(String searchString) {
        return memberRepository.findByFirstNameContainingOrLastNameContaining(searchString, searchString);
    }


    public List<String> getDistinctField(String fieldName) {
        return mongoTemplate
                .query(ParliamentMember.class)
                .distinct(fieldName)
                .as(String.class)
                .all();
    }

    public List<String> getDistinctParties() {
        return getDistinctField("party");
    }

    public List<String> getDistinctFactions() {
        return getDistinctField("faction");
    }


}
