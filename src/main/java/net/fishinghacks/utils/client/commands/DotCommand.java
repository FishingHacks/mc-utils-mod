package net.fishinghacks.utils.client.commands;

import net.minecraft.client.multiplayer.chat.ChatListener;

public abstract class DotCommand {
    public abstract String getName();
    public abstract void run(String args, ChatListener listener);
}
