package net.fishinghacks.utils.platform;

import net.fishinghacks.utils.config.ConfigBuilderImpl;
import net.fishinghacks.utils.platform.services.IConfigBuilder;
import net.fishinghacks.utils.platform.services.IPlatformHelper;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {

        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modid) {
        ModList modlist = ModList.get();
        if (modlist != null) return modlist.isLoaded(modid);
        for (var info : FMLLoader.getLoadingModList().getMods())
            if (info.getModId().equals(modid)) return true;
        return false;
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.isProduction();
    }

    @Override
    public IConfigBuilder createConfigBuilder() {
        return new ConfigBuilderImpl();
    }
}