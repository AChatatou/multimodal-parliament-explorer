package org.adch.multimodalparliamentexplorer.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
public class HtmlParser {

    public Document fetch(String url) {

            try {
                return Jsoup.connect(url)
                        .userAgent("Mozilla/5.0")
                        .timeout(10000)
                        .get();
            } catch (Exception e) {
                System.out.println("Error while fetching: " + url);
                throw new RuntimeException(e);
            }
    }
}
