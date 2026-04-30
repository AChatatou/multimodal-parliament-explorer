package org.adch.multimodalparliamentexplorer.pipeline;

import java.util.function.Function;


public class Pipeline<I, O> {

    private final Function<I, O> pipeline;

    private Pipeline(Function<I, O> pipeline) {
        this.pipeline = pipeline;
    }

    public static <T, U> Pipeline<T, U> of(PipelineStep<T, U> first) {
        return new Pipeline<>(first::process);
    }

    public <NEW_O> Pipeline<I, NEW_O> then(PipelineStep<O, NEW_O> next) {
        return new Pipeline<>(pipeline.andThen(next::process));
    }

    public O execute(I input) {
        return pipeline.apply(input);
    }
}
