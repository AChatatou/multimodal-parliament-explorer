package org.adch.multimodalparliamentexplorer.importer.dto;

import org.adch.multimodalparliamentexplorer.member.ParliamentMember;
import org.adch.multimodalparliamentexplorer.session.Session;

import java.util.List;

public record MappedImportResult(
        Session session,
        List<ParliamentMember> members
) {
}
