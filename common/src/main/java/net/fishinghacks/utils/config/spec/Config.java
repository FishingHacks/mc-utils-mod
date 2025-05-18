package net.fishinghacks.utils.config.spec;

import net.fishinghacks.utils.platform.services.IConfig;

import java.util.ArrayList;
import java.util.List;

public abstract class Config {
    private final List<Runnable> cachedConfigValues = new ArrayList<>();

    public void onLoad() {
    }

    public abstract IConfig getConfig();

    public boolean isLoaded() {
        return getConfig().isLoaded();
    }

    public String getFilename() {
        return null;
    }

    public final void addCachedValue(Runnable clearCache) {
        cachedConfigValues.add(clearCache);
    }

    public final void clearCache() {
        for (var v : cachedConfigValues) v.run();
        onLoad();
    }

    public abstract ConfigType type();
    public abstract ConfigSpec spec();

    // Note: Initially I copied a system from Mekanism where you save the config in a separate thread to avoid freezes or it not saving(?) on slower systems.
    // When I did that, however, the server was unable to exit (It stopped but then didn't exit). I have no idea why this is happening, but saving on the mainthread seems to work.
    public void save() {
        getConfig().save();
    }
}
