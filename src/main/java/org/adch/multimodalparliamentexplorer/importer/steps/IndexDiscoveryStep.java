package org.adch.multimodalparliamentexplorer.importer.steps;

import lombok.extern.slf4j.Slf4j;
import org.adch.multimodalparliamentexplorer.parser.HtmlParser;
import org.adch.multimodalparliamentexplorer.utils.UrlUtils;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;

import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class IndexDiscoveryStep {

    private final String sourceUrl;
    private int urlCount =0;

    public IndexDiscoveryStep(@Value("${app.datasource.speech-data}") String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    private String extractBaseImportUrl(String legislativePeriod) {

        var sourceDoc = HtmlParser.fetchAsync(sourceUrl).join();
        log.info("Extracting XML URLs from {}", sourceUrl);
        var speechSections = sourceDoc.select("section[data-dataloader-url]");

        return speechSections.stream()
//                .peek(sectionElement -> {
//                    System.out.println("Found element: " + sectionElement.tagName());
//                    //System.out.println("SECTION HTML:\n" + sectionElement.outerHtml());
//                })
                .filter(sectionElement -> getLegislativePeriodFromElement(sectionElement).equals(legislativePeriod))
                .map(sectionElement -> UrlUtils.getUriBase(sourceUrl) + sectionElement.attr("data-dataloader-url"))
                .limit(1)
                .collect(Collectors.joining());
    }

    private String getLegislativePeriodFromElement(Element element) {

        var heading = element.selectFirst("h2.bt-title");

        if (heading == null) {
            log.warn("No h2.bt-title element found in this section");
            return "";
        }

        String text = heading.text();
        //System.out.println("Found heading text: " + text);

        var pattern = Pattern.compile("(\\d+)\\.\\s*Wahlperiode");
        var matcher = pattern.matcher(text);

        if (matcher.find()) {
            String legislativePeriod = matcher.group(1);
            //System.out.println("Extracted: " + legislativePeriod);
            return legislativePeriod;
        }

        log.warn("Legislative Period could not be extracted from this section");
        return "";
    }
}
