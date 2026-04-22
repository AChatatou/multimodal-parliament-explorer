package org.adch.multimodalparliamentexplorer.importer;

import org.adch.multimodalparliamentexplorer.parser.XmlParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
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
public class MdbZipReader {

    private final String zipSource;
    private Document membersXmlDoc;
    private XmlParser xmlParser;


    public MdbZipReader(@Value("${app.datasource.mdb-data}") String zipSource){
        this.zipSource = zipSource;
    }


    public void fetchAndParseMemberData() {

        var httpClient = HttpClient.newHttpClient();

        var request = HttpRequest.newBuilder()
                .GET()
                .header("Accept", "application/zip")
                .uri(URI.create(zipSource))
                .build();

        try {

            var response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

            try (InputStream inputStream = response.body();
                 ZipInputStream zipIn = new ZipInputStream(new BufferedInputStream(inputStream), StandardCharsets.UTF_8)) {

                ZipEntry entry;
                while ((entry = zipIn.getNextEntry()) != null) {
                    if (entry.getName().toLowerCase().endsWith(".xml")) {
                        byte[] bytes = zipIn.readAllBytes();
                        System.out.printf("Entry: %s, bytes read: %s%n", entry.getName(), bytes.length);
                        parseContent(bytes);
                        return;
                    }
                }
            }


            throw new RuntimeException("No XML file found in ZIP");

        } catch (Exception e) {
            e.printStackTrace();
        }

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
}
