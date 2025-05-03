package net.fishinghacks.utils.client.commands;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class HelpCommand extends DotCommand {
    @Override
    public String getName() {
        return "help";
    }

    @Override
    public void run(String args, ChatListener listener) {
        CommandManager.commands.forEach((name, cmd) -> {
            Component desc = cmd.description();
            MutableComponent message = Component.literal(".").append(name);
            if (desc != null) {
                message = message.append(": ").append(desc);
            }
            Minecraft.getInstance().getChatListener().handleSystemMessage(message, false);
        });
    }
}
