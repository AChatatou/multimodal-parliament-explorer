package org.adch.multimodalparliamentexplorer.importer.dto;

import org.adch.multimodalparliamentexplorer.member.ParliamentMember;
import org.adch.multimodalparliamentexplorer.session.Session;

public record ImportResult(
        Session session,
        ParliamentMember member
) {
}
