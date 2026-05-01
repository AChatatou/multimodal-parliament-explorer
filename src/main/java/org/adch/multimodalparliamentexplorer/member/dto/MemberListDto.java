package org.adch.multimodalparliamentexplorer.member.dto;

public record MemberListDto(
        String id,
        String firstName,
        String lastName,
        String Party,
        String faction
) {
}
