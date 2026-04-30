package org.adch.multimodalparliamentexplorer.importer.tools;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.adch.multimodalparliamentexplorer.importer.dto.session.XmlUrlBatch;
import org.adch.multimodalparliamentexplorer.parser.HtmlParser;
import org.adch.multimodalparliamentexplorer.importer.util.UrlUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Getter
@Component
public class XmlIndexDiscovery {

    private final String sourceUrl;
    private String baseUrl = "";
    private int totalXmlUrlCount;
    private AtomicInteger urlsFetched = new AtomicInteger(0);
    private List<CompletableFuture<XmlUrlBatch>> batchesFutures = new ArrayList<>();
    private final AtomicInteger fetchedBatches = new AtomicInteger(0);
    private final HtmlParser htmlParser;

    private static final CompletableFuture<XmlUrlBatch> EMPTY_BATCH_FUTURE =
            CompletableFuture.completedFuture(new XmlUrlBatch(List.of()));


    public XmlIndexDiscovery(@Value("${app.datasource.speech-data}") String sourceUrl,
                             HtmlParser htmlParser) {
        this.sourceUrl = sourceUrl;
        this.htmlParser = htmlParser;
    }

    private void extractBaseImportUrl(String legislativePeriod) {

        var sourceDoc = htmlParser.fetchAndParse(sourceUrl);
        log.info("Extracting XML URLs from {}. Legislative period selected: {}", sourceUrl, legislativePeriod);
        var dataSections = sourceDoc.select("section[data-dataloader-url]");

        baseUrl = dataSections.stream()
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


    private void extractTotalXmlUrls(Document html) {
        var hitsElement = html.selectFirst("div[data-hits]");
        if (hitsElement == null) {
            log.warn("Total number of XML urls could not be read. Defaulting to 0");
            totalXmlUrlCount = 0;
            return;
        }

        try {
            totalXmlUrlCount = Integer.parseInt(hitsElement.attr("data-hits"));
        } catch (NumberFormatException e) {
            log.warn("An error occurred while parsing the url count. Defaulting to 0");
            totalXmlUrlCount = 0;
        }
    }

    private XmlUrlBatch extractXmlUrlsFromPage(Document html, Set<String> urlsToSkip) {
        var batch = new XmlUrlBatch(
                html
                .select("a[href]")
                .stream()
                .map(element -> element.attr("href").trim())
                .filter(href -> !href.isBlank() && href.endsWith(".xml"))
                .filter(href -> ! urlsToSkip.contains(href))
                .toList()
        );
        //System.out.println(batch.xmlUrls());
        urlsFetched.addAndGet(batch.batchSize());

        return batch;
    }


    public void initDiscovery(String legislativePeriod, Set<String> urlsToSkip) {
        extractBaseImportUrl(legislativePeriod);

        var firstPage = htmlParser.fetchAndParse(baseUrl);
        extractTotalXmlUrls(firstPage);

        List<CompletableFuture<XmlUrlBatch>> futures = new ArrayList<>();

        futures.add(
                CompletableFuture.completedFuture(firstPage)
                        .thenApply(document -> extractXmlUrlsFromPage(document, urlsToSkip))
        );

        IntStream.iterate(10, i -> i < totalXmlUrlCount, i -> i + 10)
                .mapToObj(i -> baseUrl + "?offset=" + i)
                .map(url -> CompletableFuture.supplyAsync(() -> htmlParser.fetchAndParse(url))
                        .thenApply(document -> extractXmlUrlsFromPage(document, urlsToSkip)))
                .forEach(futures::add);

        log.info("Selected legislative period: {}, Found {} batches of XML urls (Max batch size: 10)", legislativePeriod,  futures.size());
        log.info("Total URLs: {}. URLs to be fetched: {}", totalXmlUrlCount, urlsFetched.get());
        this.batchesFutures = futures;
    }

    public boolean hasNext() {
        return batchesFutures != null && ! batchesFutures.isEmpty() && fetchedBatches.get() < batchesFutures.size();
    }


    public CompletableFuture<XmlUrlBatch> getNextUrlBatch() {

        if (batchesFutures == null || batchesFutures.isEmpty()) {
            throw new IllegalStateException("XML urls have not been discovered");
        }

        int index = fetchedBatches.getAndIncrement();

        if (index >= batchesFutures.size()) {
            return EMPTY_BATCH_FUTURE;
        }

        return batchesFutures.get(index);
    }


    public void reset() {
        fetchedBatches.set(0);
        batchesFutures.clear();
        totalXmlUrlCount = 0;
        urlsFetched.set(0);
    }


    public List<CompletableFuture<XmlUrlBatch>> getBatchesFutures() {
        return Collections.unmodifiableList(batchesFutures);
    }

    public int getFetchedBatchesCount() {
        return fetchedBatches.get();
    }

    public int getUrlsFetched() {
        return urlsFetched.get();
    }


}
