package org.adch.multimodalparliamentexplorer.importer;

import lombok.AllArgsConstructor;
import org.adch.multimodalparliamentexplorer.importer.mapper.MemberMapper;
import org.adch.multimodalparliamentexplorer.importer.mapper.SessionMapper;
import org.adch.multimodalparliamentexplorer.importer.tools.MdbPhotoExtractor;
import org.adch.multimodalparliamentexplorer.importer.tools.MdbZipReader;
import org.adch.multimodalparliamentexplorer.importer.tools.XmlIndexDiscovery;
import org.adch.multimodalparliamentexplorer.member.MongoMemberRepository;
import org.adch.multimodalparliamentexplorer.parser.XmlParser;
import org.adch.multimodalparliamentexplorer.pipeline.Pipeline;
import org.adch.multimodalparliamentexplorer.pipeline.steps.MappingStep;
import org.adch.multimodalparliamentexplorer.pipeline.steps.PersistenceStep;
import org.adch.multimodalparliamentexplorer.pipeline.steps.XmlParseStep;
import org.adch.multimodalparliamentexplorer.session.MongoSessionRepository;
import org.adch.multimodalparliamentexplorer.session.Session;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ImporterService {

    private XmlIndexDiscovery xmlIndexDiscovery;

    private MdbZipReader mdbZipReader;

    private MdbPhotoExtractor mdbPhotoExtractor;

    private XmlParser xmlParser;

    private MemberMapper memberMapper;

    private SessionMapper sessionMapper;

    private MongoMemberRepository memberRepository;

    private MongoSessionRepository sessionRepository;


    public void initImport(String legislativePeriod){

        xmlIndexDiscovery.initDiscovery(legislativePeriod, getSavedSessionXmlUrls(legislativePeriod));

        while (xmlIndexDiscovery.hasNext()){
            Pipeline.of(new XmlParseStep(xmlParser))
                    .then(new MappingStep(sessionMapper, memberMapper, mdbZipReader, mdbPhotoExtractor))
                    .then(new PersistenceStep(sessionRepository, memberRepository))
                    .execute(xmlIndexDiscovery.getNextUrlBatch());
        }
    }

    public Set<String> getSavedSessionXmlUrls(String legislativePeriod){
        return sessionRepository
                .findByLegislativePeriod(legislativePeriod)
                .stream()
                .map(Session::getSourceXmlUrl)
                .collect(Collectors.toSet());
    }


    public int getTotalUrlsFound() {
        return xmlIndexDiscovery.getTotalXmlUrlCount();
    }

    public int getFetchedUrlsCount(){
        return xmlIndexDiscovery.getUrlsFetched();
    }

}
