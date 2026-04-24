package org.adch.multimodalparliamentexplorer.importer.util;

import java.time.format.DateTimeFormatter;

public class DateTimeFormat {

    public static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("H:mm");

    private DateTimeFormat() {}

}
