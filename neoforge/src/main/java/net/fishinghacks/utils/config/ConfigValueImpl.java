package net.fishinghacks.utils.config;

import net.fishinghacks.utils.platform.services.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec;

public record ConfigValueImpl<T>(ModConfigSpec.ConfigValue<T> inner) implements ConfigValue<T> {
    @Override
    public T getDefault() {
        return inner.getDefault();
    }

    @Override
    public void set(T value) {
        inner.set(value);
    }

    @Override
    public void clearCache() {
        inner.clearCache();
    }

    @Override
    public T getRaw() {
        return inner.getRaw();
    }

    @Override
    public T get() {
        return inner.get();
    }
}
