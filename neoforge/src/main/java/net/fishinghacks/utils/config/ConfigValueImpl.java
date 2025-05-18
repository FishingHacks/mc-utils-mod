package net.fishinghacks.utils.config;

import net.fishinghacks.utils.platform.services.ConfigValue;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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

    @Override
    public List<String> getPath() {
        return inner.getPath();
    }

    @Override
    public @Nullable String getTranslation() {
        return inner.getSpec().getTranslationKey();
    }

    @Override
    public boolean isValid(Object value) {
        return inner.getSpec().test(value);
    }
}
