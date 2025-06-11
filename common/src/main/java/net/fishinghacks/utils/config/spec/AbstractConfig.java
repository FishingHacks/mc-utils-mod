package net.fishinghacks.utils.config.spec;

import net.fishinghacks.utils.platform.services.IConfig;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract class for a config that is wrapping a platform-dependent implementation
 */
public abstract class AbstractConfig {
    private final List<Runnable> cachedConfigValues = new ArrayList<>();

    /**
     * Creates a new Platform-independent Configuration
     */
    public AbstractConfig() {
    }

    /**
     * Invoked when the config gets loaded
     */
    @SuppressWarnings("EmptyMethod")
    public void onLoad() {
    }

    /**
     * Returns the platform-specific config that was returned by the builder
     *
     * @return the config
     * @see ConfigBuilder
     * @see ConfigBuilder#build()
     */
    public abstract IConfig getConfig();

    /**
     * Check if the config is loaded
     *
     * @return if the config is loaded
     */
    public boolean isLoaded() {
        return getConfig().isLoaded();
    }

    /**
     * gets the filename of this config, if it is custom.
     * If the filename isn't custom, this will return null.
     * The default filename is {@code <modid>-<type>.toml}.
     *
     * @return the custom filename
     */
    @Nullable
    public String getFilename() {
        return null;
    }

    /**
     * Adds a procedure to invoke when the config gets updated.
     *
     * @param clearCache the procedure
     */
    public final void addCachedValue(Runnable clearCache) {
        cachedConfigValues.add(clearCache);
    }

    /**
     * Notify the config that it got loaded/updated
     */
    public final void clearCache() {
        for (var v : cachedConfigValues) v.run();
        onLoad();
    }

    /**
     * Gets the type of the config
     *
     * @return the config type
     */
    public abstract ConfigType type();

    /**
     * Gets the specification for this config, returned by the config builder.
     *
     * @return the spec
     * @see ConfigBuilder
     * @see ConfigBuilder#getSpec()
     */
    public abstract ConfigSpec spec();

    /**
     * Saves the config synchronously.
     */
    // Note: Initially I copied a system from Mekanism where you save the config in a separate thread to avoid
    // freezes or it not saving(?) on slower systems.
    // When I did that, however, the server was unable to exit (It stopped but then didn't exit). I have no idea why
    // this is happening, but saving on the mainthread seems to work.
    public void save() {
        getConfig().save();
    }
}
