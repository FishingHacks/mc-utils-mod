package net.fishinghacks.utils.commands;

import net.minecraft.client.multiplayer.chat.ChatListener;

public abstract class ArgDotCommand extends DotCommand {
    public final void run(String args, ChatListener listener) {
        this.run(args.split(" "), listener);
    }

    public abstract void run(String[] args, ChatListener listener);
}
