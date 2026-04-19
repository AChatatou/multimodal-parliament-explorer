package org.adch.multimodalparliamentexplorer.utils;

import java.net.URI;
import java.nio.file.Path;

public class UrlUtils {

    public static String getUrlFileName(String xmlUrl){
        String fileName =  Path.of(xmlUrl).getFileName().toString();

        int dotIndex = fileName.lastIndexOf('.');

        return (dotIndex == -1)
                ? fileName
                : fileName.substring(0, dotIndex);

    }

    public static String getUriBase(String uriString) {

        URI uri = URI.create(uriString);

        String root = uri.getScheme() + "://" + uri.getHost();

        if (uri.getPort() != -1)
            root += ":" + uri.getPort();

        return root;
    }
}
