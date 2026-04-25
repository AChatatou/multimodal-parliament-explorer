package org.adch.multimodalparliamentexplorer.importer.mapper;

import org.adch.multimodalparliamentexplorer.importer.dto.mdb.MdbPhoto;
import org.adch.multimodalparliamentexplorer.importer.dto.mdb.MdbZipData;
import org.adch.multimodalparliamentexplorer.member.ParliamentMember;
import org.adch.multimodalparliamentexplorer.member.ParliamentMemberPhoto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MemberMapper {

    ParliamentMemberPhoto fromMdbPhoto(MdbPhoto mdbPhoto);

    @Mapping(target = "photo", source = "mdbPhoto")
    @Mapping(target = "faction", ignore = true)   // not in source
    @Mapping(target = "id", source = "mdbZipData.id")
    ParliamentMember fromMdbZipData(MdbZipData mdbZipData, MdbPhoto mdbPhoto);
}
