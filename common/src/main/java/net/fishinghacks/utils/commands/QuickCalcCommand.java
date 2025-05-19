package net.fishinghacks.utils.commands;

import net.minecraft.client.multiplayer.chat.ChatListener;

public class QuickCalcCommand extends DotCommand {
    @Override
    public String getName() {
        return "=";
    }

    @Override
    public void run(String args, ChatListener listener) {
        CalcCommand.runCalculation(args.trim(), listener);
    }
}
