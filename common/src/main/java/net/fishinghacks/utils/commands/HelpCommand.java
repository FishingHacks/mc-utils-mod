package net.fishinghacks.utils.commands;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public class HelpCommand extends DotCommand {
    @Override
    public String getName() {
        return "help";
    }

    @Override
    public void run(String args, ChatListener listener) {
        CommandManager.commands.forEach((name, cmd) -> {
            if (I18n.exists("utils.cmd." + name + ".help")) {
                for (String line : I18n.get("utils.cmd." + name + ".help").split("\n")) {
                    Minecraft.getInstance().getChatListener().handleSystemMessage(Component.literal(line), false);
                }
            } else Minecraft.getInstance().getChatListener()
                .handleSystemMessage(Component.literal(".").append(name).withStyle(ChatFormatting.YELLOW), false);
        });
    }
}
