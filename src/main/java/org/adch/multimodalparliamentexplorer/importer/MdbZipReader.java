package org.adch.multimodalparliamentexplorer.importer;

import lombok.extern.slf4j.Slf4j;
import org.adch.multimodalparliamentexplorer.importer.dto.mdb.MdbZipData;
import org.adch.multimodalparliamentexplorer.parser.XmlParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
@Slf4j
public class MdbZipReader {

    private final String zipSource;
    private Document membersXmlDoc;
    private final XmlParser xmlParser;


    public MdbZipReader(@Value("${app.datasource.mdb-data}") String zipSource, XmlParser xmlParser){
        this.zipSource = zipSource;
        this.xmlParser = xmlParser;
        fetchAndParseMemberData(HttpClient.newHttpClient(), zipSource);
    }


    public void fetchAndParseMemberData(HttpClient httpClient, String source) {

        var request = HttpRequest.newBuilder()
                .GET()
                .header("Accept", "application/zip")
                .uri(URI.create(source))
                .build();

        try {
            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            byte[] xml = extractXmlFromZip(response.body());
            parseContent(xml);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private byte[] extractXmlFromZip(InputStream inputStream) throws IOException {
        try (ZipInputStream zipIn = new ZipInputStream(new BufferedInputStream(inputStream), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                if (entry.getName().toLowerCase().endsWith(".xml")) {
                    var bytes = zipIn.readAllBytes();
                    log.info("Entry: {}, bytes read: {}", entry.getName(), bytes.length);
                    return bytes;
                }
            }
        }
        throw new RuntimeException("No XML file found in ZIP");
    }


    private void parseContent(byte[] input) throws Exception {
        try( var bais = new ByteArrayInputStream(input)) {
            membersXmlDoc = xmlParser.fetchAndParse(bais);
        }
    }



    private Optional<Element> extractMemberElement(String id) {
        var memberNodes = membersXmlDoc.getElementsByTagName("MDB");

        for(int i = 0; i < memberNodes.getLength(); i++) {
            var memberNode = memberNodes.item(i);
            var idElement =  ((Element) memberNode).getElementsByTagName("ID").item(0);
            String memberId = idElement.getTextContent().trim();
            if (memberId.equals(id))
                return Optional.of((Element) memberNode);
        }

        return Optional.empty();
    }


    private MdbZipData extractDataFromMemberElement(Element memberElement) {
        String id = memberElement.getElementsByTagName("ID").item(0).getTextContent().trim();

        var nameElements = memberElement.getElementsByTagName("NAME");
        var nameElement = (Element) nameElements.item(nameElements.getLength()-1);
        String title = nameElement.getElementsByTagName("AKAD_TITEL").item(0).getTextContent().trim();
        String lastName = nameElement.getElementsByTagName("NACHNAME").item(0).getTextContent().trim();
        String firstName = nameElement.getElementsByTagName("VORNAME").item(0).getTextContent().trim();

        var biographyElement = (Element) memberElement.getElementsByTagName("BIOGRAFISCHE_ANGABEN").item(0);
        String party = biographyElement.getElementsByTagName("PARTEI_KURZ").item(0).getTextContent().trim();
        String occupation = biographyElement.getElementsByTagName("BERUF").item(0).getTextContent().trim();
        String biography = biographyElement.getElementsByTagName("VITA_KURZ").item(0).getTextContent().trim();

        return MdbZipData.builder()
                .id(id)
                .title(title)
                .firstName(firstName)
                .lastName(lastName)
                .party(party)
                .occupation(occupation)
                .biography(biography)
                .build();
    }


    public MdbZipData extractMemberData(String id) {
        return extractMemberElement(id)
                .map(this::extractDataFromMemberElement)
                .orElseGet(MdbZipData::emptyMdbData);
    }
}
