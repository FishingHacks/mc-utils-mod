package net.fishinghacks.utils.platform.services;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public interface IClientPlatformHelper {
    void openModlistScreen(Minecraft mc, Screen parent);
    boolean hasModlistScreen();
    void openConfigScreen();
}