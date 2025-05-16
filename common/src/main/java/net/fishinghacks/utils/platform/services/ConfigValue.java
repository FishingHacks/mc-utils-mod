package net.fishinghacks.utils.platform.services;

import java.util.function.Supplier;

public interface ConfigValue<T> extends Supplier<T> {
    T getDefault();
    void set(T value);
    void clearCache();
    /// Returns the value without caching
    T getRaw();
}
