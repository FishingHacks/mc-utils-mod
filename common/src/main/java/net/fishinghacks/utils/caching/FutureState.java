package net.fishinghacks.utils.caching;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class FutureState<T> {
    private final boolean didError;
    private final @Nullable T result;

    private FutureState(boolean didError, @Nullable T result) {
        this.didError = didError;
        this.result = result;
    }

    public static <T> FutureState<T> errored() {
        return new FutureState<>(true, null);
    }

    public static <T> FutureState<T> processing() {
        return new FutureState<>(false, null);
    }

    public static <T> FutureState<T> of(@NotNull T value) {
        return new FutureState<>(false, value);
    }

    public static <T> FutureState<T> from(CompletableFuture<T> future) {
        if (future.isCompletedExceptionally() || future.isCancelled()) return FutureState.errored();
        return new FutureState<>(false, future.getNow(null));
    }

    public boolean didError() {
        return didError;
    }

    public Optional<T> getValue() {
        return Optional.ofNullable(result);
    }

    public boolean isProcessing() {
        return !didError && result == null;
    }

    public boolean isDone() {
        return !didError && result != null;
    }
}