package net.fishinghacks.utils.client.commands;

import net.fishinghacks.utils.common.Utils;
import net.fishinghacks.utils.client.gui.configuration.ConfigurationSelectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.ChatListener;

public class ConfigCommand extends DotCommand {
    @Override
    public String getName() {
        return "config";
    }

    @Override
    public void run(String args, ChatListener listener) {
        Minecraft.getInstance().setScreen(new ConfigurationSelectionScreen(Utils.container, Minecraft.getInstance().screen));
    }
}
