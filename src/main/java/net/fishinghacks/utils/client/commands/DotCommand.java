package net.fishinghacks.utils.client.commands;

import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public abstract class DotCommand {
    public abstract String getName();
    public abstract void run(String args, ChatListener listener);
    public @Nullable Component description() { return null; }
}
