package org.adch.multimodalparliamentexplorer.speech;

public sealed interface Segment permits TextSegment, Comment {

    String text();
    default boolean isComment() {
        return false;
    }
}
