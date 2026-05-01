package org.adch.multimodalparliamentexplorer.importer.dto;

import org.adch.multimodalparliamentexplorer.member.ParliamentMember;
import org.adch.multimodalparliamentexplorer.session.Session;
import org.adch.multimodalparliamentexplorer.speech.Speech;

import java.util.List;

public record MappedImportResult(
        Session session,
        List<Speech> speeches,
        List<ParliamentMember> members
) {
}
