package net.fishinghacks.utils.config.values;

import net.fishinghacks.utils.Constants;
import net.fishinghacks.utils.config.spec.AbstractConfig;
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

/**
 * An abstract interface for making config values
 * <br/><br/>
 * NOTE FOR EXTENDERS: you have to call `finish` at the end of your constructor. if you are also abstract, let your
 * extenders know this too.
 *
 * @param <T> The type this value stores
 */
public abstract class AbstractCachedValue<T> {
    private @Nullable Set<InvalidationListener> invalidationListeners;
    private @Nullable T value;
    /**
     * Determines if the game or world have to be restarted when this value changes.
     */
    public final RestartType needsRestart;

    /**
     * NOTE FOR EXTENDERS: you have to call `finish` at the end of your constructor. if you are also abstract, let
     * your extenders know this too.
     *
     * @param config  The config this value is part of
     * @param builder The config builder used to build the config
     */
    protected AbstractCachedValue(AbstractConfig config, ConfigBuilder builder) {
        this.needsRestart = builder.type;
        config.addCachedValue(this::clearCache);
    }

    /**
     * this should be called at the end of the constructor of an extender.
     *
     * @param builder the configuration builder
     * @see #AbstractCachedValue(AbstractConfig, ConfigBuilder)
     */
    protected void finish(ConfigBuilder builder) {
        builder.register(this);
    }

    /**
     * The underlying get implementation
     *
     * @return returns the value in the config
     */
    protected abstract T doGet();

    /**
     * Returns the value without caching
     *
     * @return The value saved by the config without any caching
     */
    public abstract T getRaw();

    /**
     * Gets the default value for this config
     *
     * @return The default value
     */
    public abstract T getDefault();

    /**
     * Checks if a value can be assigned to this cached value.
     *
     * @param value The value to check
     * @return If the value is valid for this value.
     * @see #set(T)
     * @see #set(T, boolean)
     */
    public abstract boolean isValid(Object value);

    /**
     * Gets the current value
     *
     * @return The last cached value
     */
    public T get() {
        return value != null ? value : (value = doGet());
    }

    /**
     * The underlying set implementation
     *
     * @param value The value to set
     * @param save  whether to save the config or not
     */
    protected abstract void doSet(T value, boolean save);

    /**
     * Sets and saves the value to the config
     *
     * @param value The new value
     * @see #set(T, boolean)
     * @see #save()
     */
    public void set(T value) {
        set(value, true);
    }

    /**
     * Changes the cached value and possibly saves it to the config
     *
     * @param value The new value
     * @param save  Whether to save it or not
     * @see #save()
     */
    public void set(T value, boolean save) {
        this.value = null;
        doSet(value, save);
    }

    /**
     * Gets invoked when the value tries to clear the cache in the case an extending class needs to do some more action
     */
    protected abstract void doClearCache();

    /**
     * Saves the cached value to the config (Through {@link #doSet(T, boolean)})
     *
     * @see #doSet(T, boolean)
     */
    public void save() {
        doSet(value == null ? get() : value, true);
    }

    /**
     * Adds a new listener that gets called when the value changes.
     *
     * @param listener The invalidation listener
     * @see AbstractCachedValue#onInvalidate(InvalidationListener)
     */
    public void onInvalidate(InvalidationListener listener) {
        if (invalidationListeners == null) invalidationListeners = new HashSet<>();
        invalidationListeners.add(listener);
    }

    /**
     * Clears the cache and runs all invalidate listeners if {@link #get()} returned a different value.
     */
    public void clearCache() {
        doClearCache();
        T old = value;
        value = null;
        if (invalidationListeners == null || Objects.equals(old, get())) return;
        for (var listener : invalidationListeners) listener.run();
    }

    /**
     * Gets the path of the value in the config
     *
     * @return Returns the path of the key into the config
     */
    protected abstract List<String> getPath();

    /**
     * Gets a custom minecraft component translation key for this value, if it is present, returning
     * {@code null} otherwise.
     *
     * @return the translation key
     */
    protected abstract @Nullable String getTranslationKey();

    /**
     * Gets the key of this value, the path's last element.
     *
     * @return The key
     * @see #getPath()
     */
    public String getKey() {
        return getPath().getLast();
    }

    /**
     * Gets the minecraft component translation key for this value
     *
     * @return the translation key
     * @see #getNameTranslation()
     * @see #getTooltipTranslation()
     */
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

    /**
     * Gets the translation for this value
     *
     * @return the translation
     * @see #getNameTranslationKey()
     * @see #getTooltipTranslation()
     */
    public Component getNameTranslation() {
        return Component.translatable(getNameTranslationKey());
    }

    /**
     * Gets the translation of the tooltip for this value, if it exists, returning {@code null} otherwise.
     *
     * @return the translation
     * @see #getNameTranslationKey()
     * @see #getNameTranslation()
     */
    public @Nullable Component getTooltipTranslation() {
        var key = getNameTranslationKey() + ".tooltip";
        return I18n.exists(key) ? Component.translatable(key) : null;
    }

    /**
     * A function that gets called when the value gets invalidated
     */
    @FunctionalInterface
    public interface InvalidationListener extends Runnable {
    }
}
