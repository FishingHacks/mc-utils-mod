package net.fishinghacks.utils.common.config;

import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class Config {
    @Nullable
    public abstract ModConfig.Type getConfigType();

    public abstract @NotNull ModConfigSpec getModConfigSpec();

    public void onLoad() {
    }

    public boolean isLoaded() {
        return getModConfigSpec().isLoaded();
    }

    public String getFilename() {
        return null;
    }

    private final List<Runnable> cachedConfigValues = new ArrayList<>();

    public final void addCachedValue(Runnable clearCache) {
        cachedConfigValues.add(clearCache);
    }

    public final void clearCache() {
        for (var v : cachedConfigValues) v.run();
        onLoad();
    }

    // Note: Initially I copied a system from Mekanism where you save the config in a separate thread to avoid freezes or it not saving(?) on slower systems.
    // When I did that, however, the server was unable to exit (It stopped but then didn't exit). I have no idea why this is happening, but saving on the mainthread seems to work.
    public final void save() {
        getModConfigSpec().save();
    }
}
