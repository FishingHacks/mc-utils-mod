package net.fishinghacks.utils.platform;

import net.fishinghacks.utils.platform.services.IClientPlatformHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class FabricClientPlatformHelper implements IClientPlatformHelper {
    @Override
    public void openModlistScreen(Minecraft mc, Screen parent) {
    }

    @Override
    public boolean hasModlistScreen() {
        return false;
    }

    // TODO: *actual* Config
    @Override
    public void openConfigScreen() {
    }
}
