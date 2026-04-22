package org.adch.multimodalparliamentexplorer.parser;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;

@Component
public class XmlParser {

    private final DocumentBuilderFactory dbf;


    public XmlParser() {
        try {
            dbf = DocumentBuilderFactory.newDefaultInstance();
            dbf.setValidating(false);
            dbf.setNamespaceAware(true);
            dbf.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);
            //dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Failed to configure secure XML parser", e);
        }
    }


    private Document parse(org.xml.sax.InputSource source)
            throws ParserConfigurationException, IOException, SAXException {
        var builder = dbf.newDocumentBuilder();
        var parsedDoc = builder.parse(source);
        parsedDoc.normalizeDocument();
        return parsedDoc;
    }

    public Document fetchAndParse(String url)
            throws ParserConfigurationException, IOException, SAXException {
        return parse(new InputSource(url));
    }

    public Document fetchAndParse(InputStream is)
            throws ParserConfigurationException, IOException, SAXException {
        return parse(new InputSource(is));
    }


}
