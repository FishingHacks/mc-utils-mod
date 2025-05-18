package net.fishinghacks.utils.platform.services;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

public interface ConfigValue<T> extends Supplier<T> {
    T getDefault();
    void set(T value);
    void clearCache();
    /// Returns the value without caching
    T getRaw();
    default String getKey() {
        return getPath().getLast();
    }
    List<String> getPath();
    @Nullable String getTranslation();
    boolean isValid(Object value);
}
