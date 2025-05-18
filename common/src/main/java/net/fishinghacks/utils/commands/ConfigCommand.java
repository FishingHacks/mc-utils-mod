package net.fishinghacks.utils.commands;

import net.fishinghacks.utils.Constants;
import net.fishinghacks.utils.config.Configs;
import net.fishinghacks.utils.gui.configuration.ConfigSectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.ChatListener;

public class ConfigCommand extends DotCommand {
    @Override
    public String getName() {
        return "config";
    }

    @Override
    public void run(String args, ChatListener listener) {
        Constants.LOG.info("Opening config screen");
        ConfigSectionScreen.open(Minecraft.getInstance(), Configs.clientConfig);
    }
}
