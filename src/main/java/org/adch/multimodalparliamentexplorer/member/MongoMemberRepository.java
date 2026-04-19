package org.adch.multimodalparliamentexplorer.member;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoMemberRepository extends MongoRepository<ParliamentMember, String> {
}
