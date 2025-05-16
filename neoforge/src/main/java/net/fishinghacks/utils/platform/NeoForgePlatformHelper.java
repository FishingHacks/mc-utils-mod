package net.fishinghacks.utils.platform;

import net.fishinghacks.utils.UtilsMod;
import net.fishinghacks.utils.config.ConfigBuilderImpl;
import net.fishinghacks.utils.gui.configuration.ConfigurationSelectionScreen;
import net.fishinghacks.utils.platform.services.IConfigBuilder;
import net.fishinghacks.utils.platform.services.IPlatformHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.gui.ModListScreen;

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
    public void openModlistScreen(Minecraft mc, Screen parent) {
        mc.setScreen(new ModListScreen(parent));
    }

    @Override
    public boolean hasModlistScreen() {
        return true;
    }

    @Override
    public IConfigBuilder createConfigBuilder() {
        return new ConfigBuilderImpl();
    }

    @Override
    public void openConfigScreen() {
        Minecraft.getInstance()
            .setScreen(new ConfigurationSelectionScreen(UtilsMod.container, Minecraft.getInstance().screen));
    }
}