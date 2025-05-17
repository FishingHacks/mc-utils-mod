package net.fishinghacks.utils.config;

import net.fishinghacks.utils.platform.services.ConfigValue;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class ConfigValueImpl<T> implements ConfigValue<T> {
    private final Supplier<T> defaultValue;
    private final List<String> path;
    @Nullable ConfigImpl parent;
    final ValueSpec spec;
    private @Nullable T cached;

    public ConfigValueImpl(ConfigBuilderImpl parent, Supplier<T> defaultValue, List<String> path, ValueSpec spec) {
        this.defaultValue = defaultValue;
        this.path = path;
        this.spec = spec;
        parent.values.add(this);
    }

    @Override
    public T getDefault() {
        return defaultValue.get();
    }

    @Override
    public void set(T value) {
        if (parent == null || parent.loadedConfig == null || !parent.isLoaded())
            throw new IllegalStateException("Cannot set config value before config is loaded.");
        parent.loadedConfig.config().set(path, value);
        if(spec.restartType == RestartType.None) cached = value;
    }

    @Override
    public void clearCache() {
        cached = null;
    }

    @Override
    public T getRaw() {
        if (parent == null || parent.loadedConfig == null || !parent.isLoaded())
            throw new IllegalStateException("Cannot get config value before config is loaded.");
        return Objects.requireNonNull(parent.loadedConfig.config().get(path));
    }

    @Override
    public T get() {
        return cached == null ? cached = getRaw() : cached;
    }

    public List<String> getPath() {
        return path;
    }
}
