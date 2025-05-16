package net.fishinghacks.utils.commands;

import net.fishinghacks.utils.connection.ClientConnectionHandler;
import net.fishinghacks.utils.gui.cosmetics.CosmeticsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.chat.ChatListener;

public class CosmeticsCommand extends DotCommand {
    @Override
    public String getName() {
        return "cosmetics";
    }

    @Override
    public void run(String args, ChatListener listener) {
        if(ClientConnectionHandler.getInstance().isConnected()) Minecraft.getInstance().setScreen(new CosmeticsScreen(null));
    }
}
