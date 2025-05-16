package net.fishinghacks.utils.config;

import net.fishinghacks.utils.platform.services.IConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public record ConfigImpl(ModConfigSpec inner) implements IConfig {
    @Override
    public boolean isLoaded() {
        return inner.isLoaded();
    }

    @Override
    public void save() {
        inner.save();
    }
}
