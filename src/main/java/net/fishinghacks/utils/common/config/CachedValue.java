package net.fishinghacks.utils.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class CachedValue<T> {
    private final Config config;
    private final ModConfigSpec.ConfigValue<T> internalValue;
    private @Nullable Set<InvalidationListener> invalidationListeners;
    private @Nullable T value;

    private CachedValue(Config config, ModConfigSpec.ConfigValue<T> internalValue) {
        this.config = config;
        this.internalValue = internalValue;
        config.addCachedValue(this::clearCache);
    }

    public T get() {
        if(value != null) return value;
        if(!config.isLoaded()) return internalValue.getDefault();
        value = internalValue.get();
        return value;
    }

    public void onInvalidate(InvalidationListener listener) {
        if(invalidationListeners == null) invalidationListeners = new HashSet<>();
        invalidationListeners.add(listener);
    }
    public void set(T value) {
        set(value, true);
    }
    public void set(T value, boolean save) {
        internalValue.set(value);
        this.value = value;
        if(save)
            config.save();
    }

    public void clearCache() {
        internalValue.clearCache();
        T old = value;
        value = null;
        if(Objects.equals(old, get()) || invalidationListeners == null) return;
        for(var listener : invalidationListeners) listener.run();
    }

    public static <T> CachedValue<T> wrap(Config cfg, ModConfigSpec.ConfigValue<T> value) {
        return new CachedValue<>(cfg, value);
    }

    @FunctionalInterface
    public interface InvalidationListener extends Runnable {}
}
