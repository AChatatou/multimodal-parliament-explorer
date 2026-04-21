package org.adch.multimodalparliamentexplorer.session.speech;

public record Comment(String text) implements Segment {

    @Override
    public boolean isComment() {
        return true;
    }
}
