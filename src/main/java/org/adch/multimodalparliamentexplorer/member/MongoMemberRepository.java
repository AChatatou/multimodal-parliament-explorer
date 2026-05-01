package org.adch.multimodalparliamentexplorer.member;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MongoMemberRepository extends MongoRepository<ParliamentMember, String> {
    List<ParliamentMember> findByParty(String party);
    List<ParliamentMember> findByFaction(String faction);
    List<ParliamentMember> findByFirstNameContainingOrLastNameContaining(String first, String last);
}
