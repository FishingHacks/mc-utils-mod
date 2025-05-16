package net.fishinghacks.utils.commands;

import net.fishinghacks.utils.platform.ClientServices;
import net.minecraft.client.multiplayer.chat.ChatListener;

public class ConfigCommand extends DotCommand {
    @Override
    public String getName() {
        return "config";
    }

    @Override
    public void run(String args, ChatListener listener) {
        ClientServices.PLATFORM.openConfigScreen();
    }
}
