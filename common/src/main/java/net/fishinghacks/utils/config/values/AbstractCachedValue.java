package net.fishinghacks.utils.config.values;

import net.fishinghacks.utils.Constants;
import net.fishinghacks.utils.config.spec.Config;
import net.fishinghacks.utils.config.spec.ConfigBuilder;
import net.fishinghacks.utils.config.spec.RestartType;
import net.fishinghacks.utils.gui.configuration.TranslationChecker;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/// EXTENDERS: you have to call `finish` at the end of your constructor. if you are also abstract, let your extenders know this too.
public abstract class AbstractCachedValue<T> {
    private @Nullable Set<InvalidationListener> invalidationListeners;
    private @Nullable T value;
    public final RestartType needsRestart;

    /// EXTENDERS: you have to call `finish` at the end of your constructor. if you are also abstract, let your extenders know this too.
    protected AbstractCachedValue(Config config, ConfigBuilder builder) {
        this.needsRestart = builder.type;
        config.addCachedValue(this::clearCache);
    }

    protected void finish(ConfigBuilder builder) {
        builder.register(this);
    }

    protected abstract T doGet();
    /// Getting the underlying value without any checks in-between
    public abstract T getRaw();
    public abstract T getDefault();
    public abstract boolean isValid(Object value);

    public T get() {
        return value != null ? value : (value = doGet());
    }

    protected abstract void doSet(T value, boolean save);

    public void set(T value) {
        set(value, true);
    }

    public void set(T value, boolean save) {
        this.value = null;
        doSet(value, save);
    }

    protected abstract void doClearCache();

    public void save() {
        doSet(value, true);
    }

    public void onInvalidate(InvalidationListener listener) {
        if (invalidationListeners == null) invalidationListeners = new HashSet<>();
        invalidationListeners.add(listener);
    }

    public void clearCache() {
        doClearCache();
        T old = value;
        value = null;
        if (invalidationListeners == null || Objects.equals(old, get())) return;
        for (var listener : invalidationListeners) listener.run();
    }

    protected abstract List<String> getPath();

    protected abstract @Nullable String getTranslationKey();

    public String getKey() {
        return getPath().getLast();
    }

    public String getNameTranslationKey() {
        if (getTranslationKey() != null) return getTranslationKey();
        StringBuilder fullString = new StringBuilder(Constants.MOD_ID + ".configuration");
        for (var entry : getPath()) {
            fullString.append('.');
            fullString.append(entry);
        }
        return TranslationChecker.getWithFallback(fullString.toString(),
            Constants.MOD_ID + ".configuration." + getKey());
    }

    public Component getNameTranslation() {
        return Component.translatable(getNameTranslationKey());
    }

    public @Nullable Component getTooltipTranslation() {
        var key = getNameTranslationKey() + ".tooltip";
        return I18n.exists(key) ? Component.translatable(key) : null;
    }

    @FunctionalInterface
    public interface InvalidationListener extends Runnable {
    }
}
