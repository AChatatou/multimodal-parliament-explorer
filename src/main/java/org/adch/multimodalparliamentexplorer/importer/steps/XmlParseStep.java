package org.adch.multimodalparliamentexplorer.importer.steps;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.adch.multimodalparliamentexplorer.importer.model.session.*;
import org.adch.multimodalparliamentexplorer.importer.utils.DateTimeFormat;
import org.adch.multimodalparliamentexplorer.parser.XmlParser;
import org.adch.multimodalparliamentexplorer.pipeline.PipelineStep;
import org.adch.multimodalparliamentexplorer.session.speech.Comment;
import org.adch.multimodalparliamentexplorer.session.speech.Segment;
import org.adch.multimodalparliamentexplorer.session.speech.TextSegment;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@AllArgsConstructor
@Component
public class XmlParseStep implements PipelineStep<CompletableFuture<XmlUrlBatch>, CompletableFuture<List<SessionImportData>>> {

    private static final List<String> ALLOWED_P_CLASSES = List.of("J_1", "J", "O");
    private XmlParser xmlParser;


    public Optional<SessionImportData> safeExtractDataFromXml(String xmlUrl) {

        try {
            return Optional.of(extractDataFromXml(xmlUrl));
        } catch (Exception e) {
            log.error("Error parsing {}", xmlUrl, e);
            return Optional.empty();
        }

    }


    private SessionImportData extractDataFromXml(String xmlUrl) throws ParserConfigurationException, IOException, SAXException {

        var parsedDocument = xmlParser.fetchAndParse(xmlUrl);

        SessionMetadata metadata = extracSessionMetadata(parsedDocument)
                .orElseGet(SessionMetadata::emptyMetadata);

        List<SpeechImportData> dataList = new ArrayList<>();

        var speechNodes = parsedDocument.getElementsByTagName("rede");

        if (speechNodes == null || speechNodes.getLength() == 0) {
            log.warn("No speech elements were found in the file {}", xmlUrl);
            return SessionImportData.builder()
                    .xmlUrl(xmlUrl)
                    .sessionMetadata(metadata)
                    .speechesImportData(List.of())
                    .build();
        }

        for (int i = 0; i < speechNodes.getLength(); i++) {
            var node = speechNodes.item(i);

            if (!(node instanceof Element))
                continue;

            var speechElement = (Element) (node);
            var speechData = extractSpeechData(speechElement);
            dataList.add(speechData);
        }

        return SessionImportData.builder()
                .xmlUrl(xmlUrl)
                .sessionMetadata(metadata)
                .speechesImportData(dataList)
                .build();
    }


    private Optional<SessionMetadata> extracSessionMetadata(Document parsedDocument) {
        var rootNode = parsedDocument.getElementsByTagName("dbtplenarprotokoll");
        if (rootNode == null) {
            log.warn("Root element with the name dbtplenarprotokoll not found");
            return Optional.empty();
        }

        var root = (Element) (rootNode.item(0));

        String period = root.getAttribute("wahlperiode");
        String sessionNumber = root.getAttribute("sitzung-nr");
        LocalDate sessionDate = LocalDate.parse(root.getAttribute("sitzung-datum").trim(),
                DateTimeFormat.DATE_FORMAT);

        LocalTime sessionStart = LocalTime.parse(root.getAttribute("sitzung-start-uhrzeit").trim(),
                DateTimeFormat.TIME_FORMAT);

        LocalTime sessionEnd = LocalTime.parse(root.getAttribute("sitzung-ende-uhrzeit").trim(),
                DateTimeFormat.TIME_FORMAT);

        return Optional.of(SessionMetadata.builder()
                .legislativePeriod(period)
                .sessionNumber(sessionNumber)
                .date(sessionDate)
                .startTime(sessionStart)
                .endTime(sessionEnd)
                .build()
        );
    }


    private static SpeechImportData extractSpeechData(Element speechElement) {

        String id = speechElement.getAttribute("id");

        var speakerData = extractSpeakerData(speechElement);

        var segments = extractSpeechText(speechElement, speakerData.speakerId());

        return SpeechImportData.builder()
                .id(id)
                .speakerData(speakerData)
                .segments(segments)
                .build();
    }

    private static SpeakerData extractSpeakerData(Element speechElement) {

        var speakerElement = (Element) speechElement.getElementsByTagName("redner").item(0);

        String speakerId = speakerElement.getAttribute("id");

        String speakerFirstName = speakerElement.getElementsByTagName("vorname")
                .item(0)
                .getTextContent().trim();

        String speakerLastName = speakerElement.getElementsByTagName("nachname")
                .item(0)
                .getTextContent().trim();

        NodeList factions = speakerElement.getElementsByTagName("fraktion");

        String speakerFaction = factions.getLength() > 0
                ? factions.item(0).getTextContent().trim()
                : "Fraktionslos";

        return SpeakerData.builder()
                .speakerId(speakerId)
                .speakerFirstName(speakerFirstName)
                .speakerLastName(speakerLastName)
                .faction(speakerFaction)
                .build();
    }


    private static List<Segment> extractSpeechText(Element speechElement, String speakerId) {

        List<Segment> segments = new ArrayList<>();

        var children = speechElement.getChildNodes();

        boolean isReading = true;
        for (int i = 0; i < children.getLength(); i++) {
            var childNode = children.item(i);
            if (!(childNode instanceof Element childElement)) {
                continue;
            }

            String tagName = childElement.getTagName().trim();

            if (tagName.equals("kommentar"))
                segments.add(new Comment(childElement.getTextContent().trim()));

            else if (tagName.equals("name"))
                isReading = false;

            else if (isReading && tagName.equals("p")
                    && ALLOWED_P_CLASSES.contains(childElement.getAttribute("klasse")))
                segments.add(new TextSegment(childElement.getTextContent().trim()));

            else if (tagName.equals("p") && childElement.getAttribute("klasse").equals("redner")) {
                String currentSpeakerId = extractSpeakerData(childElement).speakerId();
                isReading = currentSpeakerId.equals(speakerId);
            }

        }

        return segments;
    }


    @Override
    public CompletableFuture<List<SessionImportData>> process(CompletableFuture<XmlUrlBatch> input) {

        return input
                .thenCompose(
                    xmlUrlBatch -> {
                        var futures = xmlUrlBatch.xmlUrls()
                                .stream()
                                .map(xmlUrl -> CompletableFuture.supplyAsync(() -> safeExtractDataFromXml(xmlUrl)))
                                .toList();

                        return CompletableFuture
                                .allOf(futures.toArray(new CompletableFuture[0]))
                                .thenApplyAsync(v->
                                        futures.stream()
                                                .map(CompletableFuture::join)
                                                .flatMap(Optional::stream)
                                                .toList()
                                );
                    }
                );
    }




}
