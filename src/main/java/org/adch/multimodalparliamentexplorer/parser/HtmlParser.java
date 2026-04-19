package org.adch.multimodalparliamentexplorer.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.concurrent.CompletableFuture;

public class HtmlParser {

    public static CompletableFuture<Document> fetchAsync(String url) {

        return CompletableFuture.supplyAsync(() -> {
            try {
                return Jsoup.connect(url)
                        .userAgent("Mozilla/5.0")
                        .timeout(10000)
                        .get();
            } catch (Exception e) {
                System.out.println("Error while fetching: " + url);
                throw new RuntimeException(e);
            }
        });
    }
}
