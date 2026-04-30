package org.adch.multimodalparliamentexplorer.pipeline;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;


public class AsyncPipeline<I, O> {

    private final Function<I, CompletableFuture<O>> pipeline;

    private AsyncPipeline(Function<I, CompletableFuture<O>> pipeline) {
        this.pipeline = pipeline;
    }

    public static <T, U> AsyncPipeline<T, U> of(PipelineStep<T, U> first) {
        return new AsyncPipeline<>(first::process);
    }

    public <NEW_O> AsyncPipeline<I, NEW_O> then(PipelineStep<O, NEW_O> next) {
        return new AsyncPipeline<>(input ->
                pipeline.apply(input)
                        .thenCompose(next::process)
        );
    }

    public CompletableFuture<O> execute(I input) {
        return pipeline.apply(input);
    }
}
