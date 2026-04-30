package org.adch.multimodalparliamentexplorer.importer;

import org.adch.multimodalparliamentexplorer.importer.dto.MappedImportResult;
import org.adch.multimodalparliamentexplorer.pipeline.steps.PersistenceStep;
import org.adch.multimodalparliamentexplorer.member.MongoMemberRepository;
import org.adch.multimodalparliamentexplorer.member.ParliamentMember;
import org.adch.multimodalparliamentexplorer.session.MongoSessionRepository;
import org.adch.multimodalparliamentexplorer.session.Session;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PersistenceStepTest {


    @Mock
    private MongoSessionRepository sessionRepository;

    @Mock
    private MongoMemberRepository memberRepository;

    @InjectMocks
    private PersistenceStep persistenceStep;

    @Test
    void shouldSaveSessionsAndMembers() {

        // given
        var session1 = Session.builder()
                .sessionNumber("1")
                .sessionDate(LocalDate.now())
                .legislativePeriod("legislativePeriod")
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(9, 0))
                .sourceXmlUrl("xmlUrl")
                .build();

        var member1 = ParliamentMember.builder()
                .id("id")
                .firstName("firstName")
                .lastName("lastName")
                .faction("faction")
                .build();

        var result1 = new MappedImportResult(session1, List.of(member1));

        persistenceStep.process(List.of(result1));

        verify(memberRepository).saveAll(List.of(member1));
        verify(sessionRepository).saveAll(List.of(session1));
    }
}
