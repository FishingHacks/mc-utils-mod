package net.fishinghacks.utils.client.caching;

import java.util.concurrent.CompletableFuture;

public record FutureStateHolder<T>(CompletableFuture<T> future) {
    public FutureState<T> getState() {
        return FutureState.from(future);
    }
}
