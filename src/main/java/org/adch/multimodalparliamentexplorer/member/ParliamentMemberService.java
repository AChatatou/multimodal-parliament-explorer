package org.adch.multimodalparliamentexplorer.member;

import lombok.AllArgsConstructor;
import org.adch.multimodalparliamentexplorer.member.dto.MemberListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class ParliamentMemberService {

    private MongoMemberRepository memberRepository;
    private final MongoTemplate mongoTemplate;


    public Page<MemberListDto> getAllMembers(Pageable pageable){
        return memberRepository.findAll(pageable).map(member ->
                new MemberListDto(member.getId(),
                                    member.getFirstName(),
                                    member.getLastName(),
                                    member.getParty(),
                                    member.getFaction()
                )
        );
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
