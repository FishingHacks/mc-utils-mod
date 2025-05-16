package net.fishinghacks.utils.config;

import net.fishinghacks.utils.platform.services.ConfigValue;

public final class ConfigValueImpl<T> implements ConfigValue<T> {
    private final T defaultValue;
    private T value;

    public ConfigValueImpl(T defaultValue) {
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    @Override
    public T getDefault() {
        return defaultValue;
    }

    @Override
    public void set(T value) {
        this.value = value;
    }

    @Override
    public void clearCache() {
    }

    @Override
    public T getRaw() {
        return value;
    }

    @Override
    public T get() {
        return value;
    }
}
