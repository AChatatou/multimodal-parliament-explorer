package org.adch.multimodalparliamentexplorer.pipeline;

@FunctionalInterface
public interface PipelineStep<I, O> {
    O process(I input);
}
