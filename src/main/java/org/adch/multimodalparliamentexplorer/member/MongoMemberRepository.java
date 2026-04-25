package org.adch.multimodalparliamentexplorer.member;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MongoMemberRepository extends MongoRepository<ParliamentMember, String> {
}
