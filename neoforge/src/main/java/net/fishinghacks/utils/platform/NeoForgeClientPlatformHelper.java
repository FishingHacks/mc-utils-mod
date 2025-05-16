package net.fishinghacks.utils.platform;

import net.fishinghacks.utils.UtilsMod;
import net.fishinghacks.utils.config.ConfigBuilderImpl;
import net.fishinghacks.utils.gui.configuration.ConfigurationSelectionScreen;
import net.fishinghacks.utils.platform.services.IClientPlatformHelper;
import net.fishinghacks.utils.platform.services.IConfigBuilder;
import net.fishinghacks.utils.platform.services.IPlatformHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.gui.ModListScreen;

public class NeoForgeClientPlatformHelper implements IClientPlatformHelper {

    @Override
    public void openModlistScreen(Minecraft mc, Screen parent) {
        mc.setScreen(new ModListScreen(parent));
    }

    @Override
    public boolean hasModlistScreen() {
        return true;
    }

    @Override
    public void openConfigScreen() {
        Minecraft.getInstance()
            .setScreen(new ConfigurationSelectionScreen(UtilsMod.container, Minecraft.getInstance().screen));
    }
}