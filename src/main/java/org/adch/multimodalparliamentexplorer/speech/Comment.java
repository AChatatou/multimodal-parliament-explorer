package org.adch.multimodalparliamentexplorer.speech;

public record Comment(String text) implements Segment {

    @Override
    public boolean isComment() {
        return true;
    }
}
