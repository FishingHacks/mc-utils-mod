package net.fishinghacks.utils.platform;

import net.fishinghacks.utils.UtilsMod;
import net.fishinghacks.utils.gui.nf_configuration.ConfigurationSelectionScreen;
import net.fishinghacks.utils.platform.services.IClientPlatformHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
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