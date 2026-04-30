package org.adch.multimodalparliamentexplorer.pipeline;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface PipelineStep<I, O> {
    CompletableFuture<O> process(I input);
}
