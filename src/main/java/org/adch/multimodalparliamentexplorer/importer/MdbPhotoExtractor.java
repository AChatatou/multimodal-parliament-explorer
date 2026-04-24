package org.adch.multimodalparliamentexplorer.importer;

import org.adch.multimodalparliamentexplorer.importer.model.mdb.MdbPhoto;
import org.adch.multimodalparliamentexplorer.parser.HtmlParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class MdbPhotoExtractor {

    private final String source;
    private Map<String, MdbPhoto> memberFotos;
    private final HtmlParser htmlParser;

    public MdbPhotoExtractor(@Value("${app.datasource.mdb-photos}") String photosSource, HtmlParser htmlParser) {
        this.source = photosSource;
        this.htmlParser = htmlParser;
        fetchMemberPhotos();
    }

    public void fetchMemberPhotos() {
        var firstPage = htmlParser.fetchAndParse(source);

        var hitsElement = firstPage.selectFirst("div[data-hits]");
        int resultCount = hitsElement != null ? Integer.parseInt(hitsElement.attr("data-hits")) : 0;

        List<CompletableFuture<Map<String, MdbPhoto>>> futures = new ArrayList<>();

        futures.add(CompletableFuture.completedFuture(firstPage)
                .thenApply(this::extractPhotoDataFromPage));

        IntStream stream = IntStream.iterate(12, i -> i < resultCount, i -> i + 12);

        stream.mapToObj(i -> source + "?offset=" + i)
                .map(url ->
                        CompletableFuture.supplyAsync(() -> htmlParser.fetchAndParse(url))
                        .thenApply(this::extractPhotoDataFromPage))
                .forEach(futures::add);

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        this.memberFotos = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(map -> map.values().stream())   // ignore the original id keys
                .collect(Collectors.toMap(
                        MdbPhoto::name,               // new key = name
                        obj -> obj,                      // value = object
                        (a, b) -> a,                     // ignore duplicates
                        LinkedHashMap::new
                ));
    }

    private Map<String, MdbPhoto> extractPhotoDataFromPage(Document page) {

        return page.select("a[href]")
                .stream()
                .map(element -> {
                    String title = element.attr("title");
                    String id = element.attr("data-id");

                    var imageElement = element.selectFirst("img");
                    String imageUrl = imageElement != null ? imageElement.attr("data-img-sm-retina") : "";
                    String imageAlt = imageElement != null ? imageElement.attr("alt") : "";

                    var copyrightElement = element.selectFirst("div.bt-bild-info-dialogue");
                    String text = copyrightElement != null ? copyrightElement.text() : "";

                    int index = text.indexOf("©");
                    String copyright = index >= 0 ? text.substring(index) : "";

                    return MdbPhoto.builder()
                            .id(id)
                            .name(title)
                            .url(imageUrl)
                            .altText(imageAlt)
                            .copyright(copyright)
                            .build();

                })
                .collect(Collectors.toMap(MdbPhoto::id, Function.identity()));
    }

    public MdbPhoto getMemberPhoto(String title, String firstName, String lastName) {
        String firstNameWithTitle = title.isEmpty() ? firstName: title + " " + firstName;
        String key = String.format("%s, %s", lastName, firstNameWithTitle).trim();
        return memberFotos.getOrDefault(key, null);
    }


    private Document fetchPhotosPage(String source) throws IOException, InterruptedException {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        HttpClient client = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/145.0.0.0 Safari/537.36";

        HttpRequest first = HttpRequest.newBuilder()
                .uri(URI.create("https://www.bundestag.de/"))
                .header("User-Agent", userAgent)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .GET()
                .build();

        HttpResponse<String> firstResponse = client.send(first, HttpResponse.BodyHandlers.ofString());
        System.out.println("Landing status: " + firstResponse.statusCode());

        HttpRequest second = HttpRequest.newBuilder()
                .uri(URI.create(source))
                .header("User-Agent", userAgent)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Referer", "https://www.bundestag.de/")
                .GET()
                .build();

        HttpResponse<String> secondResponse = client.send(second, HttpResponse.BodyHandlers.ofString());

        System.out.println("Ajax status: " + secondResponse.statusCode());
        System.out.println("Ajax final URI: " + secondResponse.uri());

        Document doc = Jsoup.parse(secondResponse.body(), secondResponse.uri().toString());
        System.out.println(doc.outerHtml().substring(0, Math.min(1000, doc.outerHtml().length())));
        return doc;
    }
}
